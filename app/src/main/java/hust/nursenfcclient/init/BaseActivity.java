package hust.nursenfcclient.init;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by admin on 2015/11/20.
 */
// 基本的Activity
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackCodePressed();
        }
        return false;
    }

    private int mPressedTime = 0;
    private void onBackCodePressed() {
        // 表示第一次点击
        if(mPressedTime == 0){
            Toast.makeText(this, "连续点击退出程序 ", Toast.LENGTH_SHORT).show();
            ++mPressedTime;

            new Thread(){
                @Override
                public void run() {
                    try {
                        sleep(2000);
                    } catch (Exception e) {
                        Log.e("LOG_TAG", e.toString());
                    } finally {
                        mPressedTime = 0;
                    }
                }
            };
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
}
