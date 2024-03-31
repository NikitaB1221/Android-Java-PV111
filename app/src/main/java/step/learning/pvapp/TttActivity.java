package step.learning.pvapp;

import android.graphics.Typeface;
import android.os.Bundle;
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

public class TttActivity extends AppCompatActivity {
    private boolean IsXTurn = true;
    private int ScoreX = 0;
    private int ScoreO = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ttt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
        Toast.makeText(this, IsXTurn? "It's X turn" : "It's O turn", Toast.LENGTH_SHORT).show();
    }

    private void fieldClick(View view) {
        Button field = (Button) view;
        if (!field.getText().equals("")) {
            Toast.makeText(this, "This field is already filled", Toast.LENGTH_SHORT).show();
            return;
        }
        if (IsXTurn){
            field.setText("X");
            field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            field.setTypeface(field.getTypeface(), Typeface.BOLD);
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_X_score_color));
            IsXTurn = false;
            Toast.makeText(this, "It's O turn", Toast.LENGTH_SHORT).show();
        }
        else {
            field.setText("O");
            field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            field.setTypeface(field.getTypeface(), Typeface.BOLD);
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_O_score_color));
            IsXTurn = true;
            Toast.makeText(this, "It's X turn", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("ScoreX", ScoreX);
        outState.putInt("ScoreO", ScoreO);
        outState.putBoolean("IsXTurn", IsXTurn);

        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
            outState.putCharSequence(btnId+"val",field.getText());
        }
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
            field.setText(savedInstanceState.getCharSequence(btnId+"val"));
            if (field.getText().equals("X")){
                field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                field.setTypeface(field.getTypeface(), Typeface.BOLD);
                field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_X_score_color));
            }
            else if (field.getText().equals("O")){
                field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
                field.setTypeface(field.getTypeface(), Typeface.BOLD);
                field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_O_score_color));
            }
        }
    }

    private void clearAllFields(){
        for (int i = 1; i < 10; i++) {
            String btnId = "ttt_field" + i;
            Button field = findViewById(getResources().getIdentifier(btnId, "id", getPackageName()));
            field.setText("");
            field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 36);
            field.setTypeface(field.getTypeface(), Typeface.BOLD);
            field.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ttt_secondary_color));
        }
    }
}