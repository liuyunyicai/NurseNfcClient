package hust.nursenfcclient.init;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.alarm.NurseService;
import hust.nursenfcclient.helps.WifiHelper;

/**
 * Created by admin on 2015/11/19.
 */
/** 应用加载界面 **/
public class LoadingActivity extends BaseActivity {

    private ImageView loadingImg;
    private TextView loadingProgress;
    // 加载动画时间
    private final static int DURATION_TIME = 3000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.loading_layout);

        initView();
        checkWifiState();

        initService();
    }

    // 初始化Servive
    private void initService() {
        startService(new Intent(this, NurseService.class));
    }

    // 初始化界面
    private void initView() {
        loadingImg = (ImageView) findViewById(R.id.loadingImg);
        loadingProgress = (TextView) findViewById(R.id.loadingProgress);

        loadingImg.setImageResource(R.mipmap.loading_img);

        // 组合形式使用动画
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(loadingImg, pvhX, pvhY);
        animator.setDuration(DURATION_TIME);
        // 添加动画监听器
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                boolean isDownloadedData = getSharedPreferences(MainActivity.SHAREDPR_NAME, MODE_PRIVATE)
                        .getBoolean(MainActivity.SHARED_DATA_DOWNLOADED, false);
                if (isDownloadedData) {
                    startActivity(new Intent(LoadingActivity.this, CheckInActivity.class));
                } else {
                    startActivity(new Intent(LoadingActivity.this, LogInActivity.class));
                }
                LoadingActivity.this.finish();
            }
        });
        animator.start();

        ObjectAnimator.ofFloat(loadingProgress, "scaleX", 0f, 1.0f).setDuration(DURATION_TIME).start();
    }

    // 判断WIFI状态，如果WIFI未开启，则自动开启
    private void checkWifiState() {
        WifiHelper.checkAndOPenWifi(this);
    }
}
