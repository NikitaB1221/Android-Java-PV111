package step.learning.pvapp;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TheSnakeActivity extends AppCompatActivity {

    private TextView[][] cells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_the_snake);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.the_snake_main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.the_snake_main_layout).setOnTouchListener(
                new OnSwipeListener(getApplicationContext()){

                    @Override
                    public void onSwipeBottom() {
                        Toast.makeText(TheSnakeActivity.this, "onSwipeBottom", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSwipeLeft() {
                        Toast.makeText(TheSnakeActivity.this, "onSwipeLeft", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSwipeRight() {
                        Toast.makeText(TheSnakeActivity.this, "onSwipeRight", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSwipeTop() {
                        Toast.makeText(TheSnakeActivity.this, "onSwipeTop", Toast.LENGTH_SHORT).show();
                    }
                });
        initField();
        startGame();
    }

    private void startGame(){
        cells[5][5].setText("A");
        cells[5][5].setBackgroundColor(getResources().getColor(R.color.ttt_X_score_color));

    }
    private void initField(){
        int w = 15;
        int h = 30;
        cells = new TextView[w][h];
        TableLayout gameField = findViewById(R.id.game_field);
        TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams();
        rowLayoutParams.weight = 1;
        rowLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        rowLayoutParams.height = 0;
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.weight = 1;
        layoutParams.setMargins ( 2, 2, 2, 2);
        layoutParams.height = ViewGroup. LayoutParams.MATCH_PARENT;
        for (int j = 0; j < h; j++) {
            TableRow row = new TableRow(getApplicationContext());
            row.setLayoutParams (rowLayoutParams);
            for (int i=0; i < w; i++){
                TextView textView = new TextView(getApplicationContext());
                textView.setBackgroundColor(getResources().getColor(R.color.ttt_secondary_color)); /*textView.setText("");*/
                textView.setLayoutParams (layoutParams);
                textView.setWidth(0);
                row.addView(textView);
                cells[i][j] = textView;
            }
            gameField.addView(row);
        }
    }
}