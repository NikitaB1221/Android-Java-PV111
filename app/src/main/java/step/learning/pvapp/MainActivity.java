package step.learning.pvapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.main_button_calc).setOnClickListener(this::onCalcButtonClick);
        findViewById(R.id.main_button_ttt).setOnClickListener(this::onTttButtonClick);
        findViewById(R.id.main_button_the_snake).setOnClickListener(this::onTheSnakeButtonClick);
    }

    private void onCalcButtonClick(View view){
        Intent intent = new Intent(
                this.getApplicationContext(),
                CalcActivity.class);
        startActivity(intent);
    }
    private void onTttButtonClick(View view){
        Intent intent = new Intent(
                this.getApplicationContext(),
                TttActivity.class);
        startActivity(intent);
    }
    private void onTheSnakeButtonClick(View view){
        Intent intent = new Intent(
                this.getApplicationContext(),
                TheSnakeActivity.class);
        startActivity(intent);
    }
}