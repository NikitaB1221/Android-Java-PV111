package step.learning.pvapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import step.learning.pvapp.orm.ChatMessage;
import step.learning.pvapp.services.Http;

public class ChatActivity extends AppCompatActivity {
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private LinearLayout messagesContainer;
    private EditText etAuthor;
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        new Thread(this::loadChatMessages).start();
        messagesContainer = findViewById(R.id.chat_messages_container);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::sendMessageClick);
        etAuthor = findViewById(R.id.chat_et_name);
        etMessage = findViewById(R.id.chat_et_message);
    }
    private void loadChatMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        try {
            String str = Http.getString(CHAT_URL);
            JSONObject jsonObject = new JSONObject(str);
            if (jsonObject.getInt("status") == 1) {
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    messages.add(ChatMessage.fromJson(jsonArray.getJSONObject(i)));
                }
            }
        } catch (Exception ignore) {
        }
        boolean needUpdate = false;
        for (ChatMessage message : messages) {
            if (chatMessages.stream().noneMatch(m -> m.getId().equals(message.getId()))) {
                chatMessages.add(message);
                needUpdate = true;
            }
        }
        if (needUpdate){
            runOnUiThread(this::updateMessagesView);
        }
    }
    private void updateMessagesView(){
        for (ChatMessage message : chatMessages){
            if (message.getTag() == null){TextView tv = new TextView(this);
                tv.setText(message.getText());
                tv.setTextColor(ContextCompat.getColor(this ,R.color.ttt_secondary_color));
                messagesContainer.addView(tv);
                message.setTag(tv);
            }
        }
    }
    private void sendChatMessage(ChatMessage message){
        try {
            URL url = new URL(CHAT_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection
                    .setRequestProperty("Accept", "application/json");
            connection
                    .setRequestProperty("Connection", "close");
            connection
                    .setRequestProperty("ContentType", "application/x-www-from-urlencoded");
            connection.setChunkedStreamingMode(0);
            OutputStream cpnnectionOutput = connection.getOutputStream();
            String body = String.format("author=%s&msg=%s", URLEncoder.encode(message.getAuthor(), StandardCharsets.UTF_8.name()),
                    URLEncoder.encode(message.getText(), StandardCharsets.UTF_8.name()));
            cpnnectionOutput.write(body.getBytes(StandardCharsets.UTF_8));
            cpnnectionOutput.flush();
            cpnnectionOutput.close();

            int statusCode = connection.getResponseCode();
            if (statusCode == 201){
                loadChatMessages();
                runOnUiThread(() -> Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show());
            }
            else {
                InputStream connectionInput = connection.getInputStream();
                body = Http.readStream(connectionInput);
                Log.e("sendChatMessage", "statusCode: " + statusCode);
                connectionInput.close();
            }
            connection.disconnect();
        } catch (Exception ex) {
            Log.e("sendChatMessage", "ex: " + ex.getMessage());
        }
    }
    private void sendMessageClick(View view){
        ChatMessage message = new ChatMessage();
        message.setAuthor(etAuthor.getText().toString());
        message.setText(etMessage.getText().toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendChatMessage(message);
            }
        }).start();

    }



}