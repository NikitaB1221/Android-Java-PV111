package step.learning.pvapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import step.learning.pvapp.orm.ChatMessage;
import step.learning.pvapp.services.Http;

public class ChatActivity extends AppCompatActivity {
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private static final String CHAT_URL = "https://chat.momentfor.fun/";
    private LinearLayout messagesContainer;
    private ScrollView messagesScroller;
    private EditText etAuthor;
    private EditText etMessage;
    private Drawable bgOwn;
    private Drawable bgOther;
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.UK);
    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private final Handler handler = new Handler();
    private MediaPlayer incomingMessage;
    private String author = null;
    private Animation chatVoiceAnimation;
    private ImageView chat_voice_btn;
    private boolean isVoiceOn = true;


    private void timer() {
        try {
            pool.submit(this::loadChatMessages);
        } catch (Exception ignored) {
        }
        handler.postDelayed(this::timer, 3000);
    }

    @SuppressLint("ClickableViewAccessibility")
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
        // new Thread(this::loadChatMessages).start();
        handler.post(this::timer);
        messagesContainer = findViewById(R.id.chat_messages_container);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::sendMessageClick);
        etAuthor = findViewById(R.id.chat_et_name);
        etMessage = findViewById(R.id.chat_et_message);
        bgOwn = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.chat_bg_own);
        bgOther = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.chat_bg_other);
        messagesScroller = findViewById(R.id.chat_messages_scroller);
        incomingMessage = MediaPlayer.create(this, R.raw.income);
        chat_voice_btn = findViewById(R.id.chat_voice_btn);
        chat_voice_btn.setOnClickListener(this::onChatVoiceClick);
        chatVoiceAnimation = AnimationUtils.loadAnimation(this, R.anim.chat_voice_btn_anim);
        messagesScroller.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                return v.performClick();
            } else {
                hideSoftwareKeyboard();
            }
            return true;
        });
        tryRestoreAuthor();
    }

    private void hideSoftwareKeyboard() {
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void onChatVoiceClick(View view) {
        view.startAnimation(chatVoiceAnimation);
        isVoiceOn = !isVoiceOn;
        ImageView imageView = (ImageView) view;
        if (!isVoiceOn) {
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.ttt_X_score_color), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.calc_primary_color), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void tryRestoreAuthor() {
        try (SQLiteDatabase db = openOrCreateDatabase("cnt_db", MODE_PRIVATE, null)) {
            db.execSQL("CREATE TABLE IF NOT EXISTS auth_stat(id ROWID,dtt DATETIME, author VARCHAR(64))");
            Cursor resultSet = db.rawQuery("SELECT author FROM auth_stat ORDER BY dtt DESC", null);
            if (resultSet.moveToFirst()){
                etAuthor.setText(resultSet.getString(0));
            }
            resultSet.close();
        }
    }

    private void saveLastAuthor(){
        try(SQLiteDatabase db = openOrCreateDatabase ("cnt_db", MODE_PRIVATE,  null) ){
            db.execSQL(
                     "INSERT INTO auth_stat(author, dtt) VALUES(?, CURRENT_TIMESTAMP)",
                    new String[]{etAuthor.getText().toString()});
        }
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
        messages.sort(Comparator.comparing(ChatMessage::getMoment));
        boolean needUpdate = false;
        for (ChatMessage message : messages) {
            if (chatMessages.stream().noneMatch(m -> m.getId().equals(message.getId()))) {
                chatMessages.add(message);
                needUpdate = true;
            }
        }
        if (needUpdate) {
            runOnUiThread(this::updateMessagesView);
        }
    }

    private void updateMessagesView() {
        String author = etAuthor.getText().toString();
        boolean isFirst = messagesContainer.getChildCount() == 0;
        boolean needSound = false;
        for (ChatMessage message : chatMessages) {
            if (message.getTag() == null) {
                boolean isOwn = author.equals(message.getAuthor());
                View view = messageView(message, isOwn);
                needSound |= !isOwn;
                messagesContainer.addView(view);
                message.setTag(view);
                if (needSound && !isFirst && isVoiceOn) {
                    incomingMessage.start();
                    chat_voice_btn.startAnimation(chatVoiceAnimation);
                }
            }
        }
        //
        messagesScroller.post(() -> messagesScroller.fullScroll(View.FOCUS_DOWN));
    }

    private View messageView(ChatMessage message, boolean isOwn) {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        layoutParams.gravity = isOwn ? Gravity.END : Gravity.START;
        layoutParams.setMargins(10, 15, 10, 15);
        linearLayout.setBackground(isOwn ? bgOwn : bgOther);
        linearLayout.setPadding(10, 15, 10, 15);


        TextView textView = new TextView(this);
        textView.setText(getString(R.string.chat_message_line1, dateTimeFormat.format(message.getMoment()), message.getAuthor()));
        linearLayout.addView(textView);
        textView = new TextView(this);
        textView.setText(message.getText());
        linearLayout.addView(textView);
        return linearLayout;
    }

    private void sendChatMessage(ChatMessage message) {
        try {
            URL url = new URL(CHAT_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("ContentType", "application/x-www-from-urlencoded");
            connection.setChunkedStreamingMode(0);
            OutputStream connectionOutput = connection.getOutputStream();
            String body = String.format("author=%s&msg=%s", URLEncoder.encode(message.getAuthor(), StandardCharsets.UTF_8.name()), URLEncoder.encode(message.getText(), StandardCharsets.UTF_8.name()));
            connectionOutput.write(body.getBytes(StandardCharsets.UTF_8));
            connectionOutput.flush();
            connectionOutput.close();

            int statusCode = connection.getResponseCode();
            if (statusCode == 201) {
                loadChatMessages();
                runOnUiThread(() -> Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show());
                etMessage.setText("");
            } else {
                InputStream connectionInput = connection.getInputStream();
                Http.readStream(connectionInput);
                Log.e("sendChatMessage", "statusCode: " + statusCode);
                connectionInput.close();
            }
            connection.disconnect();
        } catch (Exception ex) {
            Log.e("sendChatMessage", "ex: " + ex.getMessage());
        }
    }

    private void sendMessageClick(View view) {
        if (author == null) {
            author = etAuthor.getText().toString();
            if (author.isEmpty()) {
                Toast.makeText(this, R.string.chat_no_author_alert, Toast.LENGTH_SHORT).show();
                author = null;
                return;
            } else {
                saveLastAuthor();
                etAuthor.setEnabled(false);
            }
        }
        String msg = etMessage.getText().toString();
        if (msg.isEmpty()) {
            Toast.makeText(this, R.string.chat_no_message_alert, Toast.LENGTH_SHORT).show();
            return;
        }
        ChatMessage message = new ChatMessage();
        message.setAuthor(author);
        message.setText(msg);

        new Thread(() -> sendChatMessage(message)).start();

    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        pool.shutdownNow();
        super.onDestroy();
    }
}