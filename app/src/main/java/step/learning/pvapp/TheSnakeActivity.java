package step.learning.pvapp;

import android.content.Context;
import android.graphics.Color;
import android.icu.text.RelativeDateTimeFormatter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TheSnakeActivity extends AppCompatActivity {

    private static final Random RANDOM = new Random();
    private int fieldWidth = 15;
    private int fieldHeight = 28;
    private long period = 500;
    private TextView[][] cells;
    private Handler handler;
    private final LinkedList<Vector2> SNAKE = new LinkedList<>();
    private Vector2 FOOD;
    private int cellColorRes;
    private int fieldColorRes;
    private int snakeColorRes;
    private int foodColorRes;
    private String foodSymbol = new String(Character.toChars(0x1f34e));
    private boolean isPlaying;
    private MoveDirection moveDirection;

    private float time, bestTime, score, bestScore;
    private TextView tvTime, tvBestTime, tvScore, tvBestScore;

    private final String gameDataFileName = "saves.games";

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
                new OnSwipeListener(getApplicationContext()) {
                    @Override
                    public void onSwipeBottom() {
                        if(moveDirection == MoveDirection.left || moveDirection == MoveDirection.right) {
                            moveDirection = MoveDirection.bottom;
                        }
                    }
                    @Override
                    public void onSwipeLeft() {
                        if(moveDirection == MoveDirection.top || moveDirection == MoveDirection.bottom) {
                            moveDirection = MoveDirection.left;
                        }
                    }
                    @Override
                    public void onSwipeRight() {
                        if(moveDirection == MoveDirection.top || moveDirection == MoveDirection.bottom) {
                            moveDirection = MoveDirection.right;
                        }
                    }
                    @Override
                    public void onSwipeTop() {
                        if(moveDirection == MoveDirection.left || moveDirection == MoveDirection.right) {
                            moveDirection = MoveDirection.top;
                        }
                    }
                });
        cellColorRes  = getResources().getColor( R.color.the_snake_cell, getTheme() ) ;
        fieldColorRes = getResources().getColor( R.color.the_snake_background, getTheme() ) ;
        snakeColorRes = getResources().getColor( R.color.the_snake_snake, getTheme() ) ;
        foodColorRes  = getResources().getColor( R.color.the_snake_food, getTheme() ) ;

        tvTime = findViewById( R.id.the_snake_tv_time );
        tvBestTime = findViewById( R.id.the_snake_tv_best_time );
        tvScore = findViewById( R.id.the_snake_tv_score );
        tvBestScore = findViewById( R.id.the_snake_tv_best_score );

        handler = new Handler();
        initField();
        startGame();
    }
    private void loadGameData() {
        try( FileInputStream fis = openFileInput( gameDataFileName ) ) {
            DataInputStream reader = new DataInputStream( fis ) ;
            bestScore = reader.readFloat() ;
            bestTime = reader.readFloat() ;
            reader.close();
            updateLabels(true);
            Log.i("loadGameData", "Loaded " + bestScore + " " + bestTime ) ;
        }
        catch (IOException e) {
            Log.e("loadGameData", e.getMessage() ) ;
        }
    }
    private void saveGameData() {
        /* Файлова система Андроїд "видає" кожному застосунку приватну директорію.
         *  Доступ до неї для програми не обмежений, але можна її видаляти з налаштувань
         *  пристрою. Доступ для інших програм - тільки з правами адміністратора */
        try( FileOutputStream fos = openFileOutput( gameDataFileName, Context.MODE_PRIVATE ) ) {
            DataOutputStream writer = new DataOutputStream( fos ) ;
            writer.writeFloat( bestScore );
            writer.writeFloat( bestTime );
            writer.flush();
            writer.close();
            Log.i("saveGameData", "Saved " + bestScore + " " + bestTime ) ;
        }
        catch (IOException e) {
            Log.e("saveGameData", e.getMessage() ) ;
        }
    }
    private void update() {
        Vector2 newHead = SNAKE.getFirst().copy();

        // перераховуємо нову позицію голови в залежності від напряму руху
        switch (moveDirection){
            case top:    newHead.setY( newHead.getY() - 1 ) ; break;
            case bottom: newHead.setY( newHead.getY() + 1 ) ; break;
            case left:   newHead.setX( newHead.getX() - 1 ) ; break;
            case right:  newHead.setX( newHead.getX() + 1 ) ; break;
        }
        // перевіряємо, що не вийшли за межі поля
        if( newHead.getX() < 0 || newHead.getX() >= fieldWidth
                || newHead.getY() < 0 || newHead.getY() >= fieldHeight ) {
            gameOver();
            return;
        }

        // вставляємо нову голову
        SNAKE.addFirst( newHead );
        // та зарисовуємо комірку поля
        cells[newHead.getX()][newHead.getY()].setBackgroundColor( snakeColorRes );

        // перевіряємо що нова позиція - це їжа
        if(newHead.getX() == FOOD.getX() && newHead.getY() == FOOD.getY()) {
            cells[FOOD.getX()][FOOD.getY()].setText("");
            do {   // перегенеровуємо позицію їжі
                FOOD.setX( RANDOM.nextInt( fieldWidth ) );
                FOOD.setY( RANDOM.nextInt( fieldHeight ) ) ;
            } while( isInSnake(FOOD) );

            cells[FOOD.getX()][FOOD.getY()].setText(foodSymbol);
            score += 100;
        }
        else {
            Vector2 tail = SNAKE.removeLast();
            // стираємо старий хвіст - зафарбовуємо у колір комірки
            cells[tail.getX()][tail.getY()].setBackgroundColor( cellColorRes );
            score += 1;
        }
        time += (float)period / 1000;
        updateLabels(false);
        if(isPlaying) {
            handler.postDelayed(this::update, period);
        }
    }
    private void updateLabels(boolean forceUpdate) {
        tvScore.setText( String.valueOf( (int)score ) );
        tvTime.setText( getString( R.string.the_snake_template, (int)time ) ) ;
        if( time > bestTime ) {
            bestTime = time ;
            forceUpdate = true;
        }
        if( score > bestScore ) {
            bestScore = score;
            forceUpdate = true;
        }
        if( forceUpdate ) {
            tvBestTime.setText( getString( R.string.the_snake_template, (int)bestTime ) ) ;
            tvBestScore.setText( String.valueOf( (int)bestScore ) );
        }
    }
    private void startGame() {
        loadGameData();
        // стираємо залишкові асети
        if(FOOD != null) {
            cells[FOOD.getX()][FOOD.getY()].setText("");
        }
        for( Vector2 tail : SNAKE ) {
            cells[tail.getX()][tail.getY()].setBackgroundColor( cellColorRes ) ;
        }
        // Відновлюємо стартову позицію
        FOOD = new Vector2(5,5);
        cells[FOOD.getX()][FOOD.getY()].setText(foodSymbol);
        SNAKE.clear();
        SNAKE.add( new Vector2(5, 15 ) ) ;
        SNAKE.add( new Vector2(5, 16 ) ) ;
        SNAKE.add( new Vector2(5, 17 ) ) ;
        for( Vector2 tail : SNAKE ) {
            cells[tail.getX()][tail.getY()].setBackgroundColor( snakeColorRes ) ;
        }
        isPlaying = true;
        moveDirection = MoveDirection.left;
        handler.postDelayed( this::update, period ) ;
    }
    private void initField() {
        cells = new TextView[fieldWidth][fieldHeight];

        TableLayout gameField = findViewById( R.id.game_field );
        gameField.setBackgroundColor( fieldColorRes ) ;

        TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams();
        rowLayoutParams.weight = 1;
        rowLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        rowLayoutParams.height = 0;

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.weight = 1;
        layoutParams.setMargins(2,2,2,2);
        layoutParams.height =  ViewGroup.LayoutParams.MATCH_PARENT;

        for (int j = 0; j < fieldHeight; j++) {
            TableRow row = new TableRow(getApplicationContext());
            row.setLayoutParams( rowLayoutParams );
            for (int i = 0; i < fieldWidth; i++) {
                TextView textView = new TextView(getApplicationContext());
                textView.setBackgroundColor( cellColorRes );
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(layoutParams);
                textView.setWidth(0);
                row.addView(textView);
                cells[i][j] = textView;
            }
            gameField.addView(row);
        }
    }
    private boolean isInSnake(Vector2 vector) {
        return SNAKE.stream().anyMatch(v -> v.getX() == vector.getX() && v.getY() == vector.getY());
    }
    private void gameOver() {
        new AlertDialog.Builder(   // формування модального діалогу за паттерном "Builder"
                TheSnakeActivity.this,
                androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert   // Widget_AppCompat_ButtonBar_AlertDialog
        )
                .setTitle(R.string.the_snake_title)
                .setMessage(R.string.the_snake_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)   // неможна закрити без натиску кнопки
                .setPositiveButton(R.string.the_snake_pb, (dialog, buttonIndex) -> startGame())
                .setNegativeButton(R.string.the_snake_nb, (dialog, buttonIndex) -> finish())
                .show();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Log.d("snakeActivity", "onPause");
        isPlaying = false;
        saveGameData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log.d("snakeActivity", "onResume");
        isPlaying = true;
    }


    static class Vector2 {
        int x;
        int y;

        public Vector2 copy() {
            return new Vector2(x,y);
        }

        public Vector2(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    enum MoveDirection {
        left,
        right,
        top,
        bottom
    }
}