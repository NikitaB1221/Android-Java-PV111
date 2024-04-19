package step.learning.pvapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AnimActivity extends AppCompatActivity {

    private Animation alphaAnimation;
    private Animation rotateAnimation;
    private Animation rotate2Animation;
    private Animation scale1Animation;
    private Animation scale2Animation;
    private Animation translateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_anim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_alpha);
        alphaAnimation.reset();
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        rotateAnimation.reset();
        rotate2Animation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate2);
        rotate2Animation.reset();
        scale1Animation = AnimationUtils.loadAnimation(this, R.anim.anim_scale1);
        scale1Animation.reset();
        scale2Animation = AnimationUtils.loadAnimation(this, R.anim.anim_scale2);
        scale2Animation.reset();
         translateAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_translate);
         translateAnimation.reset();

        findViewById(R.id.anim_alpha_view).setOnClickListener(this::alphaClicked);
        findViewById(R.id.anim_rotate_view).setOnClickListener(this::rotateClicked);
        findViewById(R.id.anim_rotate2_view).setOnClickListener(this::rotate2Clicked);
        findViewById(R.id.anim_scale_view).setOnClickListener(this::scale1Clicked);
        findViewById(R.id.anim_scale2_view).setOnClickListener(this::scale2Clicked);
        findViewById(R.id.anim_translate_view).setOnClickListener(this::translateClicked);

        SQLiteDatabase db = openOrCreateDatabase("cnt_db",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS anim_stat(id ROWID,dtt datetime)");
        db.execSQL("INSERT INTO anim_stat(dtt) VALUES(CURRENT_TIMESTAMP)");
        Cursor resultSet = db.rawQuery("SELECT * FROM anim_stat",null);
        StringBuilder str = new StringBuilder();
        boolean hasData = resultSet.moveToFirst();
        while (hasData) {
            str.append(resultSet.getString(1)).append("\n");
            hasData = resultSet.moveToNext();
        }
        Toast.makeText(this, str.toString(), Toast.LENGTH_SHORT).show();
        resultSet.close();
        db.close();
    }

    private void alphaClicked(View view) {
        view.startAnimation(alphaAnimation);
    }
    private void rotateClicked(View view) {
        view.startAnimation(rotateAnimation);
    }
    private void rotate2Clicked(View view) {
        view.startAnimation(rotate2Animation);
    }
    private void scale1Clicked(View view) {
        view.startAnimation(scale1Animation);
    }
    private void scale2Clicked(View view) {
        view.startAnimation(scale2Animation);
    }
    private void translateClicked(View view) {
        view.startAnimation(translateAnimation);
    }
}

