package hust.nursenfcclient.alarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.database.UpdateDbStateThread;
import hust.nursenfcclient.helps.DateHelper;
import hust.nursenfcclient.helps.NFC_Helper;

/**
 * Created by admin on 2015/12/5.
 */
public class NurseService extends Service {
    private AlarmManager alarmManager;
    private SharedPreferences sharedPreferences;
    private PendingIntent alarmIntent;

    public static int ALARM_STATE;
    private static final int ALARM_CLOSED = -1;
    private static final int ALARM_OPENED = 1;

    private String alarmTime;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sharedPreferences = getSharedPreferences(MainActivity.SHAREDPR_NAME, MODE_PRIVATE);

//        Toast.makeText(NurseService.this, "Service启动成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // ==== 处理消息事件 ==== //
    public void onEventMainThread(AlarmEvent event) {
        alarmTime = sharedPreferences.getString(MainActivity.SHARED_ALARM_TIME, MainActivity.DEFAULT_ALARM_TIME);
        switch (event.getAction()) {
            case AlarmEvent.ACTION_START_ALARM:
                if (!alarmTime.equals(MainActivity.DEFAULT_ALARM_TIME)) {
                    onStartAlarm();
                }
                break;

            case AlarmEvent.ACTION_RESTART_ALARM:
                if (!alarmTime.equals(MainActivity.DEFAULT_ALARM_TIME)) {
                    onRestartAlarm();
                }
                break;

            case AlarmEvent.ACTION_CANCEL_ALARM:
                onCancelAlarm();
                Log.i("LOG_TAG", "提醒功能关闭成功！");
                break;

            case AlarmEvent.ACTION_ALARM_TIME_IN:
                onAlarmTimeIn(event.getData());
                break;

            case AlarmEvent.ACTION_STRAT_UPDATE_DB:
                if (!UpdateDbStateThread.isUpdateDbThreadRunning())
                    new Thread(new UpdateDbStateThread(this)).start();
                break;

            case AlarmEvent.ACTION_CLOSE_UPDATE_DB:
                UpdateDbStateThread.closeUpdateDbThread();
                break;

        }
    }

    // 开启Alarm
    private void onStartAlarm() {

        if ((alarmTime != null) && (!alarmTime.equals(MainActivity.DEFAULT_ALARM_TIME))) {
            int alarmType = AlarmManager.RTC_WAKEUP; // 设备休眠的话，唤醒设备
            String curTime = DateHelper.getCurTime();
            // 当前时间必须小于闹钟时间
            if (curTime.compareTo(alarmTime) <= 0) {
                int alarm_interval = DateHelper.getMsInterval(curTime, alarmTime);

                // 设置可操作的PendingIntent
                Intent intent = new Intent(AlarmEvent.ALARM_ACTION);
                intent.putExtra(AlarmEvent.ALARM_DATA, getAlarmData());
                intent.putExtra(AlarmEvent.ALARM_TYPE, AlarmReceiver.ALARM_TYPE_TIMEIN);

                alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

                Log.d(MainActivity.LOG_TAG, "alarm_interval ==" + alarm_interval);
                alarmManager.set(alarmType, System.currentTimeMillis() + alarm_interval * 1000, alarmIntent);
                Toast.makeText(NurseService.this, "提醒设置成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 重启ALARM
    private void onRestartAlarm() {
        // 先关闭之前的ALARM
        onCancelAlarm();
        // 在开启新的ALARM
        onStartAlarm();
    }

    // 取消ALARM
    private void onCancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
        }
    }

    // 弹出TIME_IN提醒Notification
    private void onAlarmTimeIn(String dataStr) {
        // 获取NotificationManager
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);

        Intent notiIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notiIntent, 0);
        builder.setSmallIcon(R.mipmap.alarm_icon)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("提醒")
                .setContentText(dataStr)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // 设置点击自动清除
                .setLights(Color.RED, 0, 1) // 设置提示灯的颜色
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE); // 设置震动

        manager.notify(0, builder.build());
    }

    // 查询数据库，获得ALARM需要提供的数据
    private String getAlarmData() {
        String datas = "病人温度测量提醒！";
        return datas;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
