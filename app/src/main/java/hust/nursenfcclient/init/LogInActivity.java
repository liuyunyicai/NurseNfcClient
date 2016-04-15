package hust.nursenfcclient.init;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;


import java.lang.ref.WeakReference;

import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.database.NurseNFCDatabaseHelper;
import hust.nursenfcclient.network.NetWorkHelper;
import hust.nursenfcclient.setting.NetSetActivity;

/**
 * Created by admin on 2015/11/20.
 */
/*登录界面*/
public class LogInActivity extends BaseActivity implements View.OnClickListener {
    private ImageView logoImg;
    private EditText idEditText;
    private Button loginBt;
    private TextView setNetText;

    // 下载时的弹出框popupWindow及其参数
    private PopupWindow popupWindow;
    private View popView;
    private TextView downloadHintText;
    private NumberProgressBar download_progress_bar;

    // 数据相关参数
    private NurseNFCDatabaseHelper dbHelper;

    // 数据接收的百分比
    public static volatile float dataRecvPrecent = 0;

    private String DEFAULT_NURSEID = "N10000";
    private DownloadHandler downloadHandler;

    private SharedPreferences sharedPreferences;
    private String nurseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        // 创建数据库
        dbHelper = NurseNFCDatabaseHelper.getInstance(getApplicationContext());
        sharedPreferences = getSharedPreferences(MainActivity.SHAREDPR_NAME, MODE_PRIVATE);
        initView();
    }

    private <T> T $(int resId) {
        return (T) findViewById(resId);
    }

    private <T> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }


    private void initView() {

        logoImg = $(R.id.logoImg);
        idEditText = $(R.id.idEditText);
        loginBt = $(R.id.loginBt);
        setNetText = $(R.id.setNetText);

        String nurseId = sharedPreferences.getString(MainActivity.SHARED_NURSE_ID, MainActivity.DEFAULT_NURSE_ID);
        idEditText.setText(nurseId);
        idEditText.setSelection(1);

        loginBt.setOnClickListener(this);
        setNetText.setOnClickListener(this);
        // 设置下划线
        setNetText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        setNetText.getPaint().setAntiAlias(true);//抗锯齿

        downloadHandler = new DownloadHandler(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setNetText:
                startActivity(new Intent(this, NetSetActivity.class));
                break;
            case R.id.loginBt:
                String input = idEditText.getText().toString().trim();
                if (input.equals("")) {
                    Toast.makeText(LogInActivity.this, R.string.empty_edittext_hint, Toast.LENGTH_SHORT).show();
                } else {
                    nurseId = input;
                    initPopWinodw(view);
                }
                break;
        }
    }

    // 弹出popupWindow
    private void initPopWinodw(View view) {
        popView = LayoutInflater.from(this).inflate(R.layout.download_pop_layout, null, false);

        downloadHintText = $(popView, R.id.downloadHintText);
        download_progress_bar = $(popView, R.id.download_progress_bar);

        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);

        ObjectAnimator animator = ObjectAnimator.ofFloat(popView, "alpha", 0.0f, 1.0f);
        animator.setDuration(1000);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 开启下载任务
                new Thread(new NetWorkHelper(LogInActivity.this, nurseId , downloadHandler,
                        NetWorkHelper.CONNECT_SERVER_AND_GET_RECV)).start();
            }
        });
    }

    public static final int PUBLISH_PROGRESS = 1;  // 从服务器中下载数据库数据
    public static final int DOWNLOAD_DB_POST = 2;  // 从服务器下载数据结果
    public static final int GET_IMAGE_EXCUTE = 3;  // 从服务器下载图片
    public static final int GET_IMAGE_PROGRESS = 4;  // 从服务器下载图片进程
    public static final int POST_EXCUTE = 5;         // 数据加载完成
    public static final int WRONG_NURSE_ID = 6;         // 没有该nurseID

    private class DownloadHandler extends Handler {
        private WeakReference<LogInActivity> weakReference;

        public DownloadHandler(LogInActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LogInActivity activity = weakReference.get();
            activity.handleMessage(msg);
        }
    }

    // 处理Handler事件
    private int photos_num = 0;

    public void handleMessage(Message msg) {
        Bundle bundle = null;
        boolean recv_success = false;
        switch (msg.what) {
            // 从服务器中下载数据库数据
            case PUBLISH_PROGRESS:
                bundle = msg.getData();
                int progress = bundle.getInt(NetWorkHelper.RECV_PROGRESS);
                // 更新进度条
                if (download_progress_bar != null)
                    download_progress_bar.setProgress(progress / 2);

                Log.w(MainActivity.LOG_TAG, "RECV_PROGRESS ==" + progress);
                break;

            // 从服务器下载数据结果
            case DOWNLOAD_DB_POST:
                bundle = msg.getData();
                recv_success = bundle.getBoolean(NetWorkHelper.RECV_SUCCESS); // 判断接收是否成功逻辑
                Log.w("LOG_TAG", String.valueOf(recv_success));

                downloadHintText.setText(recv_success ? R.string.downloaded_hint_text : R.string.downloaded_hint_text_failed);
                downloadHintText.setTextColor(getResources().getColor(recv_success ?
                        R.color.colorAccent : R.color.yellow));
                break;

            // 从服务器下载图片
            case GET_IMAGE_EXCUTE:
                photos_num = (Integer)msg.obj;
                downloadHintText.setTextColor(getResources().getColor(R.color.gray));
                String text = getResources().getString(R.string.get_image_hint);

                downloadHintText.setText(text +  "1/" + photos_num);
                break;

            // 从服务器下载图片进程
            case GET_IMAGE_PROGRESS:
                int recved_count = (Integer)msg.obj;
                recved_count++;
                String text1 = getResources().getString(R.string.get_image_hint);
                downloadHintText.setText(text1 +  recved_count + "/" + photos_num);

                int progress1 = 50 + (int)(((double)recved_count / photos_num) * 50);
                // 更新进度条
                if (download_progress_bar != null)
                    download_progress_bar.setProgress(progress1);

                break;

            // 数据加载完成
            case POST_EXCUTE:
                recv_success = (Boolean) msg.obj; // 判断接收是否成功逻辑
                Log.w("LOG_TAG", String.valueOf(recv_success));
                onPostExecute(recv_success);
                break;

            case WRONG_NURSE_ID:
                Toast.makeText(LogInActivity.this, R.string.downloaded_wrong_nurse_id, Toast.LENGTH_SHORT).show();
                ObjectAnimator animator = ObjectAnimator.ofFloat(popView, "alpha", 1.0f, 0.0f);
                animator.setDuration(1000);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        try {
                            if (popupWindow != null)
                                popupWindow.dismiss();
                        } catch (Exception e) {
                            Log.e(MainActivity.LOG_TAG, e.toString());
                        }
                    }
                });

                break;
        }

    }
    // 数据接收完成后的响应
    private void onPostExecute(final boolean isSuccess) {
        if (downloadHintText != null) {
            // 保存数据下载状态
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(MainActivity.SHARED_DATA_DOWNLOADED, isSuccess);
            editor.putString(MainActivity.SHARED_NURSE_ID, nurseId);
            editor.commit();

            if (isSuccess) {
                downloadHintText.setText(R.string.downloaded_hint_into_db_success);
                downloadHintText.setTextColor(getResources().getColor(R.color.colorAccent));
            } else {
                downloadHintText.setText(R.string.downloaded_hint_text_failed);
                downloadHintText.setTextColor(getResources().getColor(R.color.yellow));
            }

        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(popView, "alpha", 1.0f, 0.0f);
        animator.setDuration(500);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    // 跳转到下一个Activity
                    if ((!isSuccess) && (popupWindow != null))
                        popupWindow.dismiss();
                } catch (Exception e) {
                    Log.e(MainActivity.LOG_TAG, e.toString());
                }

                if (isSuccess)
                    LogInActivity.this.finish();
            }
        });

        if (isSuccess) {
            startActivity(new Intent(LogInActivity.this, CheckInActivity.class));
//            this.finish();
        }
    }
}
