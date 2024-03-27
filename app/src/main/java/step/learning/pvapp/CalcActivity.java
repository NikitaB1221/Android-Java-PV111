package step.learning.pvapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalcActivity extends AppCompatActivity {
    private TextView tvHistory;
    private TextView tvResult;

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
        cClick(null);
    }

    private void cClick(View view) {
        tvResult.setText(R.string.calc_btn_0);
    }
    private void digitClick(View view) {
        String result = tvResult.getText().toString();
        if (result.length() >= 10){
            return;
        }
        if (result.equals(getString(R.string.calc_btn_0))){
            result = "";
        }
        result += ((Button)view).getText();
        tvResult.setText(result);
    }
}



// Напомни учителю про toastы( всплывающие уведомления )