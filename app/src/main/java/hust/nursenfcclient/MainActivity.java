package hust.nursenfcclient;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.alarm.AlarmEvent;
import hust.nursenfcclient.helps.CircleImageView;
import hust.nursenfcclient.helps.FileHelper;
import hust.nursenfcclient.imageloader.ImageHelper;
import hust.nursenfcclient.house.HouseInfoFragment;
import hust.nursenfcclient.database.NurseNFCDatabaseHelper;
import hust.nursenfcclient.helps.NFC_Helper;
import hust.nursenfcclient.init.CheckInActivity;
import hust.nursenfcclient.init.LogInActivity;
import hust.nursenfcclient.setting.NetSetActivity;
import hust.nursenfcclient.network.NetWorkHelper;
import hust.nursenfcclient.network.ServicesHelper;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener , View.OnClickListener{
    public static final String LOG_TAG = "LOG_TAG";
    private Toolbar toolbar; // 顶部导航栏
    private CircleImageView nurse_photo_img;

    private FragmentManager fragmentManager;
    private HouseInfoFragment houseInfoFragment; // 记载病房列表界面

    // 下载时的弹出框popupWindow及其参数
    private PopupWindow popupWindow;
    private View popView;
    private TextView downloadHintText;
    private NumberProgressBar download_progress_bar;

    private int popWindowState;
    private static final int STATE_UPLOAD = 0;
    private static final int STATE_EXIT = 1;

    private ContextMenuDialogFragment mMenuDialogFragment; // 菜单列表

    // sharedPreference名字
    public static final String SHAREDPR_NAME = "nursenfc_shared";

    // SHARED的相关参数及默认值
    public static final String SHARED_NURSE_ID = "nurse_id";
    public static final String DEFAULT_NURSE_ID = "N";

    public static final String SHARED_NURSE_PHOTOURI = "nurse_photo";

    public static final String SHARED_DATA_DOWNLOADED = "data_downloaded";
    public static final boolean DEFAULT_DATA_DOWNLOADED = false;

    public static final String SHARED_SERVER_IP = "server_ip";
    public static final String DEFAULT_SERVER_IP = "115.156.187.146";

    public static final String SHARED_SERVER_PORT = "port";
    public static final String DEFAULT_SERVER_PORT = "8088";

    public static final String SHARED_ALARM_INTERVAL = "alarm_interval";
    public static final int DEFAULT_ALARM_INTERVAL = 15;

    public static final String SHARED_ALARM_TIME = "alarm_time";
    public static final String DEFAULT_ALARM_TIME = "null";
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final int DEFAULT_READ_TIMES = 0;

    public static final String SHARED_DELAY_TIME = "delay_time";
    public static final long DEFAULT_DELAY_TIME = 30 * 60 * 1000;
    public static final long DELAY_MINUTE_TIME = 60 * 1000;
    public static final int MIN_DALAY_MINUTE = 10;
    public static final int MAX_DALAY_MINUTE = 600;


    // 上传成功标志
    public static final int UPLOAD_DATA_TO_SERVER_SUCCESS = 2; // 数据发送服务器成功
    public static final int UPLOAD_DATA_TO_SERVER_FAIL = 3; // 数据发送服务器失败
    public static final int UPLOAD_WAITING_ACK = 4; // 数据发送服务器失败
    public static final int CLEAR_DB_SUCCESS = 5; // 清空本地数据库成功
    public static final int CLEAR_DB_FAILURE = 6; // 清空本地数据失败
    public static final int CLEARING_DB = 7; // 正在清空本地数据
    public static final int FINISH_ACTIVTY = 8; // 结束本应用

    public static final int UPLOAD_SUCCESS = 0;
    public static final int UPLOAD_FAIL = -1;
    private MainHandler myHandler;

    private int count = 0;
    private SharedPreferences sharedPreference;
    private LayoutInflater inflater;


    // ===== NFC相关 ==== //
    private NFC_Helper nfc_helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        myHandler = new MainHandler(this);
        sharedPreference = getSharedPreferences(SHAREDPR_NAME, MODE_PRIVATE);
        inflater = LayoutInflater.from(this);

        boolean isDownloadedData = getSharedPreferences(MainActivity.SHAREDPR_NAME, MODE_PRIVATE)
                .getBoolean(MainActivity.SHARED_DATA_DOWNLOADED, false);
        // 如果已经登录并且数据已经下载完毕，则直接开启
        if (isDownloadedData) {
            initNFC();
            initView();
            initMenuFragment();
        } else {
            // 否则需要跳转到
            Toast.makeText(MainActivity.this, R.string.need_login_hint, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogInActivity.class));
            this.finish();
        }
    }

    // 初始化NFC相关参数
    private void initNFC() {
        nfc_helper = NFC_Helper.getInstance(getApplicationContext());
        nfc_helper.checkAndOpenNFC();
        nfc_helper.initnfc();
    }

    // 刷新界面
    @Override
    protected void onResume() {
        super.onResume();
        showHouseInfo();
        nfc_helper.enableForegroundDispatch(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfc_helper.disableForegroundDispatch(this);
    }

    /**
     * 初始化界面
     **/
    private void initView() {
        // 设置顶部导航栏
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        nurse_photo_img = (CircleImageView) findViewById(R.id.nurse_photo_img);

        // 设置用户头像
        String photoUri = sharedPreference.getString(SHARED_NURSE_PHOTOURI, "");
        ImageHelper.setImageFitXY(nurse_photo_img, photoUri);

        nurse_photo_img.setOnClickListener(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                HouseInfoFragment.curPos = 0;
//                showHouseInfo();
                onUpload();
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
    }



    // 显示病房列表界面
    private void showHouseInfo() {
//        if (houseInfoFragment == null)
        houseInfoFragment = new HouseInfoFragment();
        fragmentManager.beginTransaction().replace(R.id.mian_container, houseInfoFragment).commitAllowingStateLoss();
    }

    private static final int CLOSE_MENU = 0;
    private static final int ALARM_MENU = 1;
    private static final int DELAY_MENU = 2;
    private static final int NET_MENU = 3;
    private static final int SELF_MENU = 4;
    private static final int UPLOAD_MENU = 5;
    private static final int EXIT_MENU = 6;

    // ====== 设置菜单 ===== //
    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }

    private List<MenuObject> getMenuObjects() {

        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.mipmap.icn_close);

        Resources res = getResources();
        MenuObject alarm = new MenuObject(res.getString(R.string.menu_alarm_hint));
        alarm.setResource(R.mipmap.alarm_icon);

        MenuObject delay = new MenuObject(res.getString(R.string.menu_delay_hint));
        delay.setResource(R.mipmap.delay_icon);

        MenuObject net = new MenuObject(res.getString(R.string.menu_net_hint));
        net.setResource(R.mipmap.net_icon);

        MenuObject self = new MenuObject(res.getString(R.string.menu_self_hint));
        self.setResource(R.mipmap.icn_3);

        MenuObject upload = new MenuObject(res.getString(R.string.menu_upload_hint));
        upload.setResource(R.mipmap.upload_icon);

        MenuObject exit = new MenuObject(res.getString(R.string.menu_exit_hint));
        exit.setResource(R.mipmap.icn_5);

        menuObjects.add(close);
        menuObjects.add(alarm);
        menuObjects.add(delay);
        menuObjects.add(net);
        menuObjects.add(self);
        menuObjects.add(upload);
        menuObjects.add(exit);

        return menuObjects;
    }

    // ==== 创建菜单及响应 ==== //
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMenuItemClick(View clickedView, int position) {
        switch (position) {
            case CLOSE_MENU:
                break;
            case ALARM_MENU:
                onAlarmSet();
                break;
            case DELAY_MENU:
                onDelaySet();
                break;
            case NET_MENU:
                startActivity(new Intent(this, NetSetActivity.class));
                break;
            case SELF_MENU:
                startActivity(new Intent(this, CheckInActivity.class));
                break;
            case UPLOAD_MENU:
                onUpload();
                break;
            case EXIT_MENU:
                onExit();
                break;
        }
    }

    public static final int FIFTEEN_MINUTES = 15;
    public static final int HALF_HOUR = 30;
    public static final int HOUR = 60;
    public static final int CLOSED = 0;
    private int[] intervals = {FIFTEEN_MINUTES, HALF_HOUR, HOUR, CLOSED};

    private SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    private SimpleDateFormat minFormat = new SimpleDateFormat("mm");
    private SimpleDateFormat the_dateFormat = new SimpleDateFormat("yyyy-MM-dd ");

    // === 设置Alarm响应 ==== //
    private void onAlarmSet() {
        View view;
        final TimePicker timePicker;
        try {
            // 初始化事件选择器
            view = LayoutInflater.from(this).inflate(R.layout.alarm_layout, null);
            timePicker = (TimePicker) view.findViewById(R.id.alarmTimePicker);
            Date date = new Date(System.currentTimeMillis());
            final int hour = Integer.valueOf(hourFormat.format(date));
            final int minute = Integer.valueOf(minFormat.format(date));
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);

            //dialog参数设置
            AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
            builder.setTitle("当前提醒时间：\n" + getAlarmSet()); //设置标题
            builder.setIcon(R.mipmap.alarm_icon);//设置图标，图片id即可
            builder.setView(view);

            builder.setPositiveButton(R.string.ensure_change_txt, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int curHour = timePicker.getCurrentHour();
                    int curMin = timePicker.getCurrentMinute();
                    setAlarmTime(curHour, curMin);
                }
            });

            builder.setNeutralButton(R.string.alarm_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelAlarm();
                }
            });

            builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception e) {
            Log.e(LOG_TAG, "onAlarmSet >>" + e.toString());
        }
    }

    private void onDelaySet() {
        //dialog参数设置
        AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器
        View contentView = inflater.inflate(R.layout.delay_set_layout, null);
        final EditText delayTimeEdit = (EditText) contentView.findViewById(R.id.delayTimeEdit);
        long delay_time = sharedPreference.getLong(MainActivity.SHARED_DELAY_TIME, MainActivity.DEFAULT_DELAY_TIME);
        delayTimeEdit.setText(String.valueOf(delay_time / DELAY_MINUTE_TIME));
        delayTimeEdit.setSelection(String.valueOf(delay_time / DELAY_MINUTE_TIME).length());

        builder.setTitle("温度测量参数设置"); //设置标题
        builder.setView(contentView);
        builder.setIcon(R.mipmap.delay_icon);//设置图标，图片id即可

        // 点击确认修改按钮
        builder.setPositiveButton(R.string.ensure_change_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = delayTimeEdit.getText().toString().trim();

                if ((text == null) || (text.length() == 0)) {
                    Toast.makeText(MainActivity.this, R.string.wrong_delay_time_hint, Toast.LENGTH_SHORT).show();
                } else {
                    int delay_minute = Integer.valueOf(text);

                    if (delay_minute > MAX_DALAY_MINUTE) {
                        Toast.makeText(MainActivity.this, R.string.max_delay_time_hint, Toast.LENGTH_SHORT).show();
                    } else if (delay_minute < MIN_DALAY_MINUTE) {
                        Toast.makeText(MainActivity.this, R.string.min_delay_time_hint, Toast.LENGTH_SHORT).show();
                    } else {
                        // 修改默认时间间隔
                        SharedPreferences.Editor editor = sharedPreference.edit();
                        editor.putLong(SHARED_DELAY_TIME, delay_minute * DELAY_MINUTE_TIME);
                        editor.commit();

                        Toast.makeText(MainActivity.this, R.string.delay_time_change_success, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void setAlarmTime(int hour, int minute) {
        String alarm_time = "";
        String this_date = the_dateFormat.format(new Date(System.currentTimeMillis()));
        String this_hour = String.format("%02d", hour);
        String this_min = String.format("%02d", minute);
        alarm_time += this_date + this_hour + ":" + this_min + ":00";

        // 暂存之前的AlarmTime状态
        String last_alarm_time = sharedPreference.getString(SHARED_ALARM_TIME, DEFAULT_ALARM_TIME);

        SharedPreferences.Editor editor = sharedPreference.edit();
        if (!alarm_time.equals(""))
            editor.putString(SHARED_ALARM_TIME, alarm_time);
        editor.commit();

        // 表示之前未开启
        if (last_alarm_time.equals(DEFAULT_ALARM_TIME)) {
            EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_START_ALARM));
        } else { // 表示之前已开启
            EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_RESTART_ALARM));
        }

    }

    // 取消闹钟功能
    private void cancelAlarm() {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(SHARED_ALARM_TIME, DEFAULT_ALARM_TIME);
        editor.commit();

        EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_CANCEL_ALARM));
    }


    // 获取当前设置闹钟时间
    private String getAlarmSet() {
        String alarm_time = sharedPreference.getString(SHARED_ALARM_TIME, DEFAULT_ALARM_TIME);

        if (alarm_time.equals(DEFAULT_ALARM_TIME))
            return "未开启提醒功能";
        return alarm_time;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nurse_photo_img:
                startActivity(new Intent(this, CheckInActivity.class));
                break;
        }
    }

    private class MainHandler extends Handler {
        private WeakReference<MainActivity> weakReference;

        public MainHandler(MainActivity activity) {
            weakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            weakReference.get().handleMessage(msg);
        }
    }

    // ==== 处理消息事件 ==== //
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case UPLOAD_DATA_TO_SERVER_SUCCESS:
                setPopText(R.string.uploading_hint, R.color.red);
                new UploadTask().execute();
                myHandler.sendEmptyMessageDelayed(UPLOAD_WAITING_ACK, 800);
                break;
            case UPLOAD_DATA_TO_SERVER_FAIL:
                setPopText(R.string.upload_fail_hint, R.color.orange);
                closePopWindow();
                break;
            case UPLOAD_WAITING_ACK:
                setPopText(R.string.uploading_data_transfered_hint, R.color.gray);
                break;

            case UPLOAD_SUCCESS:
                if (popWindowState == STATE_UPLOAD) {
                    setPopText(R.string.upload_success_hint, R.color.red);
                    count = 99;
                    closePopWindow();
                } else if (popWindowState == STATE_EXIT) {
                    try {
                        setPopText(R.string.upload_exit_success, R.color.red);
                        // 清空数据库
                        NurseNFCDatabaseHelper nurseNFCDatabaseHelper = NurseNFCDatabaseHelper.getInstance(getApplicationContext());
                        // 插入数据前注意清空数据
                        nurseNFCDatabaseHelper.clearAllTableDatas();

                        // 清除文件夹中的文件数据
                        List<String> photoUris = nurseNFCDatabaseHelper.getPhotosUri();
                        FileHelper.clearFiles(photoUris);

                        myHandler.sendEmptyMessageDelayed(CLEAR_DB_SUCCESS, 500);

                        // 这里注意关闭ALARM
                        EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_CANCEL_ALARM));
                        // 关闭数据库维护线程
                        EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_CLOSE_UPDATE_DB));
                    } catch(Exception e) {}
                }

                break;
            case UPLOAD_FAIL:
                setPopText(R.string.upload_fail_hint, R.color.colorPrimary);
                closePopWindow();
                break;

            case CLEARING_DB:
                break;

            case CLEAR_DB_FAILURE:
                setPopText(R.string.upload_exit_fail, R.color.colorPrimary);
                closePopWindow();
                break;
            case CLEAR_DB_SUCCESS:
                // 清除shared数据
                clearSharedData();

                closePopWindowAndFinish();
                break;

            case FINISH_ACTIVTY:
                this.finish();
                break;
        }
    }

    // 清除shared数据
    private void clearSharedData() {
        // 注意将IP地址保存下来
        SharedPreferences.Editor editor = sharedPreference.edit();
        String temp_ip = sharedPreference.getString(MainActivity.SHARED_SERVER_IP, MainActivity.DEFAULT_SERVER_IP);
        editor.clear();
        editor.putString(MainActivity.SHARED_SERVER_IP, temp_ip);
        editor.commit();
    }


    // 点击upload响应
    private void onUpload() {
        //先new出一个监听器，设置好监听
        DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case Dialog.BUTTON_POSITIVE:
                        popWindowState = STATE_UPLOAD;
                        initPopWinodw();
                        break;
                    case Dialog.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        //dialog参数设置
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle(R.string.upload_title); //设置标题
        builder.setMessage(R.string.upload_hint); //设置内容
        builder.setIcon(R.mipmap.warning_icon);//设置图标，图片id即可
        builder.setPositiveButton(R.string.upload_txt, dialogOnclicListener);
        builder.setNegativeButton(R.string.cancel_txt, dialogOnclicListener);
        builder.create().show();
    }

    // 弹出popupWindow
    private void initPopWinodw() {
        popView = LayoutInflater.from(this).inflate(R.layout.download_pop_layout, null, false);

        downloadHintText = $(popView, R.id.downloadHintText);
        download_progress_bar = $(popView, R.id.download_progress_bar);

        downloadHintText.setText(R.string.uploading_hint);

        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        popupWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);

        ObjectAnimator animator = ObjectAnimator.ofFloat(popView, "alpha", 0.0f, 1.0f);
        animator.setDuration(1000);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 开启上传数据线程
                String nurse_id = getSharedPreferences(MainActivity.SHAREDPR_NAME, Context.MODE_PRIVATE)
                        .getString(ServicesHelper.NURSE_ID, "N10000");

                NetWorkHelper nethelper = new NetWorkHelper(getApplicationContext(),
                        nurse_id, myHandler, NetWorkHelper.UPLOAD_DATA_TO_SERVER);

                new Thread(nethelper).start();
            }
        });
    }

    private ObjectAnimator getCloseAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(popView, "alpha", 1.0f, 0.0f);
        animator.setDuration(2000);
        animator.start();
        return animator;
    }

    // 关闭popWIndow
    private void closePopWindow() {
        ObjectAnimator animator = getCloseAnimator();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    // 跳转到下一个Activity
                    if (popupWindow != null)
                        popupWindow.dismiss();
                } catch(Exception e) {
                }
            }
        });
    }

    // 关闭popWindow并且关闭应用
    private void closePopWindowAndFinish() {
        try {
            // 跳转到下一个Activity
            if (popupWindow != null)
                popupWindow.dismiss();
        } catch(Exception e) {
        }

        this.finish();
    }

    private void setPopText(int stringId, int colorId) {
        downloadHintText.setText(stringId);
        downloadHintText.setTextColor(getResources().getColor(colorId));
    }

    // ===== 退出应用响应 ===== //
    private void onExit() {
        //先new出一个监听器，设置好监听
        DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case Dialog.BUTTON_POSITIVE:
                        popWindowState = STATE_EXIT;
                        initPopWinodw();

                        break;
                    case Dialog.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        //dialog参数设置
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle(R.string.exit_title); //设置标题
        builder.setMessage(R.string.exit_hint); //设置内容
        builder.setIcon(R.mipmap.warning_icon);//设置图标，图片id即可
        builder.setPositiveButton(R.string.exit_txt, dialogOnclicListener);
        builder.setNegativeButton(R.string.cancel_txt, dialogOnclicListener);
        builder.create().show();
    }

    // 清除相关缓存
    private void clearAllData () {
        // 清除SharedPreference
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.clear();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mMenuDialogFragment != null && mMenuDialogFragment.isAdded()) {
            mMenuDialogFragment.dismiss();
        } else {
//            super.onBackPressed();
            onBackCodePressed();
        }
    }

    // ===== 双次点击退出应用 ==== //
    private int mPressedTime = 0;

    private void onBackCodePressed() {
        // 表示第一次点击
        if (mPressedTime == 0) {
            Toast.makeText(this, "连续点击退出程序 ", Toast.LENGTH_SHORT).show();
            ++mPressedTime;

            new Thread() {
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
            MainActivity.this.finish();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private <T> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    private class UploadTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            count = 0;
            try {
                while (count <= 100) {
                    Thread.sleep(100);
                    publishProgress(count++);
                }

            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            download_progress_bar.setProgress(values[0]);
        }
    }
}
