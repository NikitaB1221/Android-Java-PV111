package step.learning.pvapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory;
    private TextView tvResult;
    private final static int MAX_DIGIT = 10;
    private boolean needClear = false;

    @SuppressLint("DiscouragedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calc);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        for (int i = 0; i < 10; i++) {
            String btnId = "calc_btn_" + i;
            findViewById(
                    getResources().getIdentifier(
                            btnId,
                            "id",
                            getPackageName()
                    )).setOnClickListener(this::digitClick);
        }

        tvHistory = findViewById(R.id.calc_tv_history);
        tvResult = findViewById(R.id.calc_tv_result);

        tvResult.setText(R.string.calc_btn_0);

        findViewById(R.id.calc_btn_c).setOnClickListener(this::cClick);
        findViewById(R.id.calc_btn_inverse).setOnClickListener(this::inverseClick);
        cClick(null);
    }

    private void cClick(View view) {
        tvResult.setText(R.string.calc_btn_0);
    }

    private void inverseClick(View view){
        needClear = true;
        String result = tvResult.getText().toString();
        double x = Double.parseDouble(result);
        if (x == 0.0){
            tvResult.setText("Inf");
            Toast.makeText(this, "Infinitesimal value", Toast.LENGTH_LONG).show();
            return;
        }
        x = 1.0 / x;
        String output = x == (int)x ? String.valueOf((int)x) : String.valueOf(x);
        if (output.length() > MAX_DIGIT) {
            output = output.substring(0, MAX_DIGIT);
        }
        tvResult.setText(output);
        output = "1 / " + result + " = ";
        tvHistory.setText(output);
    }

    private void digitClick(View view) {
        String result;
        if (needClear){
            result = getString(R.string.calc_btn_0);
            tvHistory.setText("");
            needClear = false;
        }
        else result = tvResult.getText().toString();

        if (result.length() >= MAX_DIGIT) {
            return;
        }
        if (result.equals(getString(R.string.calc_btn_0))) {
            result = "";
        }
        result += ((Button) view).getText();
        tvResult.setText(result);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("savedResult", tvResult.getText());
        outState.putCharSequence("savedHistory", tvHistory.getText());
        outState.putBoolean("needClear", needClear);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvResult.setText(savedInstanceState.getCharSequence("savedResult"));
        tvHistory.setText(savedInstanceState.getCharSequence("savedHistory"));
        needClear = savedInstanceState.getBoolean("needClear");
    }
}