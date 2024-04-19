package step.learning.pvapp;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class TttActivity extends AppCompatActivity {
    private boolean IsXTurn = true;
    private int ScoreX = 0;
    private int ScoreO = 0;
    private LinkedList<turnListInfo> turnList;
    private Handler handler;
    private final long period = 1000;
    private int xTime, oTime;
    private TextView tvXTime, tvOTime;
    private boolean isEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        clearAllFields();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ttt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ttt_main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        xTime = 0;
        oTime = 0;
        tvXTime = findViewById(R.id.xTime);
        tvOTime = findViewById(R.id.oTime);
        handler = new Handler();
        handler.postDelayed(this::update, period);
        findViewById(R.id.ttt_main_layout).setOnTouchListener(new OnSwipeListener(getApplicationContext()) {
            @Override
            public void onSwipeBottom() {

                super.onSwipeBottom();
            }

            @Override
            public void onSwipeLeft() {
                cancelLastMove();
            }

            @Override
            public void onSwipeRight() {

                super.onSwipeRight();
            }

            @Override
            public void onSwipeTop() {

                super.onSwipeTop();
            }
        });
        turnList = new LinkedList<>();
        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            @SuppressLint("DiscouragedApi") Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
            field.setOnClickListener(this::fieldClick);
        }
        clearAllFields();
        ((TextView) findViewById(R.id.ttt_X_label)).setText("X: ");
        ((TextView) findViewById(R.id.ttt_O_label)).setText("O: ");
        ((TextView) findViewById(R.id.ttt_O_score_label)).setText(String.valueOf(ScoreO));
        ((TextView) findViewById(R.id.ttt_X_score_label)).setText(String.valueOf(ScoreX));
        if (savedInstanceState == null)
            Toast.makeText(this, IsXTurn ? "It's X turn" : "It's O turn", Toast.LENGTH_SHORT).show();
    }

    private void randomFirstTurn() {
        String API_URL = "https://www.random.org/integers/?num=1&min=0&max=1&col=1&base=10&format=plain&rnd=new";
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            int randomNumber = Integer.parseInt(result.toString().trim());
            IsXTurn = randomNumber != 0;
        } catch (Exception ex) {
            Log.e("randomFirstTurn", "randomFirstTurn: " + ex.getMessage());
        }
    }

    private void update() {
        if (IsXTurn) {
            xTime += (int) period / 1000;
            tvXTime.setText(getString(R.string.ttt_time_template, xTime));
        } else {
            oTime += (int) period / 1000;
            tvOTime.setText(getString(R.string.ttt_time_template, oTime));
        }
        int winState = winCheck();
        if (winState == 1 || winState == 2) {
            isEnd = true;
            if (winState == 1) {
                ScoreX += 1;
                ((TextView) findViewById(R.id.ttt_X_score_label)).setText(String.valueOf(ScoreX));
            } else {
                ScoreO += 1;
                ((TextView) findViewById(R.id.ttt_O_score_label)).setText(String.valueOf(ScoreO));
            }
            new AlertDialog.Builder(
                    TttActivity.this,
                    androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
                    .setTitle(winState == 1 ? "X" : "O" + R.string.ttt_ad_title)
                    .setMessage(R.string.ttt_ad_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ttt_pb, (dialog, buttonIndex) -> clearAllFields())
                    .setNegativeButton(R.string.ttt_nb, (dialog, buttonIndex) -> finish())
                    .show();
        }
        if (!isEnd) handler.postDelayed(this::update, period);
    }

    private void cancelLastMove() {
        if (!turnList.isEmpty()) {
            turnListInfo lastTurn = turnList.getFirst();
            Toast.makeText(this, "Last move canceled! Now it's " + turnList.getFirst().value + " turn", Toast.LENGTH_SHORT).show();
            Button field = findViewById(lastTurn.fieldId);
            IsXTurn = field.getText().equals("X");
            field.setText("");
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_secondary_color));
            turnList.removeFirst();
        } else Toast.makeText(this, "There's nothing to cancel here", Toast.LENGTH_SHORT).show();

    }

    private void fieldClick(View view) {
        Button field = (Button) view;
        if (!field.getText().equals("")) {
            Toast.makeText(this, "This field is already filled", Toast.LENGTH_SHORT).show();
            return;
        }
        if (IsXTurn) {
            field.setText("X");
            field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            field.setTypeface(field.getTypeface(), Typeface.BOLD);
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_X_score_color));
            IsXTurn = false;
            Toast.makeText(this, "It's O turn", Toast.LENGTH_SHORT).show();
            turnList.addFirst(new turnListInfo(field.getId(), "X"));
        } else {
            field.setText("O");
            field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            field.setTypeface(field.getTypeface(), Typeface.BOLD);
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_O_score_color));
            IsXTurn = true;
            Toast.makeText(this, "It's X turn", Toast.LENGTH_SHORT).show();
            turnList.addFirst(new turnListInfo(field.getId(), "O"));
        }
    }

    ArrayList<turnListInfo> lLAL;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("ScoreX", ScoreX);
        outState.putInt("ScoreO", ScoreO);
        outState.putBoolean("IsXTurn", IsXTurn);

        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
            outState.putCharSequence(btnId + "val", field.getText());
        }
        lLAL = new ArrayList<>(turnList);
        outState.putParcelableArrayList("turnList", lLAL);
        outState.putInt("xTime", xTime);
        outState.putInt("oTime", oTime);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ScoreX = savedInstanceState.getInt("ScoreX");
        ScoreO = savedInstanceState.getInt("ScoreO");
        IsXTurn = savedInstanceState.getBoolean("IsXTurn");
        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
            field.setText(savedInstanceState.getCharSequence(btnId + "val"));
            if (field.getText().equals("X")) {
                field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                field.setTypeface(field.getTypeface(), Typeface.BOLD);
                field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_X_score_color));
            } else if (field.getText().equals("O")) {
                field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                field.setTypeface(field.getTypeface(), Typeface.BOLD);
                field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_O_score_color));
            }
        }
        xTime = savedInstanceState.getInt("xTime");
        oTime = savedInstanceState.getInt("oTime");
        tvXTime.setText(getString(R.string.ttt_time_template, xTime));
        tvOTime.setText(getString(R.string.ttt_time_template, oTime));
        turnList = new LinkedList<>(Objects.requireNonNull(savedInstanceState.getParcelableArrayList("turnList")));
        Toast.makeText(this, IsXTurn ? "It's X turn" : "It's O turn", Toast.LENGTH_SHORT).show();
    }

    private void clearAllFields() {
        randomFirstTurn();
        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
            field.setText("");
            field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            field.setTypeface(field.getTypeface(), Typeface.BOLD);
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_secondary_color));
            turnList = new LinkedList<>();
        }
    }

    private int winCheck() {
        /*Lines*/
        if (((Button) findViewById(R.id.ttt_field1)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field2)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field3)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field1)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field2)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field3)).getText().equals("O")) {
            return 2;
        }
        if (((Button) findViewById(R.id.ttt_field4)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field6)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field4)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field6)).getText().equals("O")) {
            return 2;
        }
        if (((Button) findViewById(R.id.ttt_field7)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field8)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field9)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field7)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field8)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field9)).getText().equals("O")) {
            return 2;
        }
        /*Columns*/
        if (((Button) findViewById(R.id.ttt_field1)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field4)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field7)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field1)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field4)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field7)).getText().equals("O")) {
            return 2;
        }
        if (((Button) findViewById(R.id.ttt_field2)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field8)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field2)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field8)).getText().equals("O")) {
            return 2;
        }
        if (((Button) findViewById(R.id.ttt_field3)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field6)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field9)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field3)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field6)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field9)).getText().equals("O")) {
            return 2;
        }
        /*Diagonals*/
        if (((Button) findViewById(R.id.ttt_field1)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field9)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field1)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field9)).getText().equals("O")) {
            return 2;
        }
        if (((Button) findViewById(R.id.ttt_field3)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("X")
                && ((Button) findViewById(R.id.ttt_field7)).getText().equals("X")) {
            return 1;
        } else if (((Button) findViewById(R.id.ttt_field3)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field5)).getText().equals("O")
                && ((Button) findViewById(R.id.ttt_field7)).getText().equals("O")) {
            return 2;
        }
        return 0;
    }

    private static class turnListInfo implements Parcelable {
        private int fieldId;
        private String value;

        public int getFieldId() {
            return fieldId;
        }

        public turnListInfo(int fieldId, String value) {
            this.fieldId = fieldId;
            this.value = value;
        }

        public void setFieldId(int fieldId) {
            this.fieldId = fieldId;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        protected turnListInfo(Parcel in) {
            fieldId = in.readInt();
            value = in.readString();
        }

        public final Creator<turnListInfo> CREATOR = new Creator<turnListInfo>() {
            @Override
            public turnListInfo createFromParcel(Parcel in) {
                return new turnListInfo(in);
            }

            @Override
            public turnListInfo[] newArray(int size) {
                return new turnListInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(fieldId);
            dest.writeString(value);
        }
    }
}