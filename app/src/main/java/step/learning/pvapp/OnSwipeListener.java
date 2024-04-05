package step.learning.pvapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OnSwipeListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context) {
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeBottom() {}
    public void onSwipeLeft() {}
    public void onSwipeRight() {}
    public void onSwipeTop() {}

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int MIN_DISTANCE = 30;
        private static final int MIN_VELOCITY = 30;

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            boolean isDispatched = false;
            try {
                float dx = e2.getX() - e1.getX();
                float dy = e2.getY() - e1.getY();
                if (Math.abs(dx) > Math.abs(dy)) {
                    //  Horizontal move
                    if (Math.abs(dx) >= MIN_DISTANCE && velocityX >= MIN_VELOCITY) {
                        if (dx > 0) {  //  right
                            onSwipeRight();
                        } else {  //  left
                            onSwipeLeft();
                        }
                        isDispatched = true;
                    }
                } else {
                    //  Vertical move
                    if (Math.abs(dy) >= MIN_DISTANCE && Math.abs(velocityY) >= MIN_VELOCITY) {
                        if (dy > 0) {  //  bot
                            onSwipeBottom();
                        } else {  //  top
                            onSwipeTop();
                        }
                    }
                    isDispatched = true;
                }
            } catch (Exception ignore) {
            }

            return isDispatched;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }
    }
}
