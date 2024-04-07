package step.learning.pvapp;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class TttActivity extends AppCompatActivity {
    private boolean IsXTurn = true;
    private int ScoreX = 0;
    private int ScoreO = 0;
    private LinkedList<turnListInfo> turnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ttt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ttt_main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.ttt_main_layout).setOnTouchListener(new OnSwipeListener(getApplicationContext()) {
            @Override
            public void onSwipeBottom() {

            }

            @Override
            public void onSwipeLeft() {
                cancelLastMove();
            }

            @Override
            public void onSwipeRight() {

            }

            @Override
            public void onSwipeTop() {

            }
        });
        turnList = new LinkedList<>();
        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
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

    private void cancelLastMove() {
        if (!turnList.isEmpty()) {
            turnListInfo lastTurn = turnList.getFirst();
            Toast.makeText(this, "Last move canceled! Now it's " + turnList.getFirst().value + " turn", Toast.LENGTH_SHORT).show();
            Button field = (Button) findViewById(lastTurn.fieldId);
            if (field.getText().equals("X")) IsXTurn = true;
            else IsXTurn = false;
            field.setText("");
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_secondary_color));
            turnList.removeFirst();
        }
        else
            Toast.makeText(this, "There's nothing to cancel here", Toast.LENGTH_SHORT).show();

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
        lLAL = new ArrayList<turnListInfo>(turnList);
        outState.putParcelableArrayList("turnList", lLAL);
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
        turnList = new LinkedList<turnListInfo>(Objects.requireNonNull(savedInstanceState.getParcelableArrayList("turnList")));
        Toast.makeText(this, IsXTurn ? "It's X turn" : "It's O turn", Toast.LENGTH_SHORT).show();
    }

    private void clearAllFields() {
        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
            field.setText("");
            field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            field.setTypeface(field.getTypeface(), Typeface.BOLD);
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_secondary_color));
        }
    }

    private class turnListInfo implements Parcelable {
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