package hust.nursenfcclient.init;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import hust.nursenfcclient.R;

/**
 * Created by admin on 2015/11/20.
 */
public class MySwitchButton extends TextView implements View.OnClickListener{
    private boolean isClosed = true;

    ObjectAnimator offAnimator, openAnimator;

    public MySwitchButton(Context context) {
        super(context);
    }

    public MySwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setBackGround();

        offAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 0.0f);
        offAnimator.setDuration(500);

        openAnimator = ObjectAnimator.ofFloat(this, "scaleX", 0.0f, 1.0f);
        openAnimator.setDuration(500);

        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 0.0f, 1.0f);
        showAnimator.setDuration(1000);
        showAnimator.start();


        offAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                isClosed = !isClosed;
                MySwitchButton.this.setBackGround();

                openAnimator.start();
            }
        });
    }

    private void setBackGround() {
        setBackgroundResource(isClosed ? R.mipmap.switch_closed_icon : R.mipmap.switch_open_icon);
    }

    public void setBackGround(boolean isClosed) {
        if (isClosed != this.isClosed()) {
            changeBackGroundState();
        } else {
            setIsClosed(isClosed);
            setBackGround();
        }
    }

    public void changeBackGroundState() {
        offAnimator.start();
    }

    public MySwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View view) {
        changeBackGroundState();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return false;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setIsClosed(boolean isClosed) {
        this.isClosed = isClosed;
    }
}
