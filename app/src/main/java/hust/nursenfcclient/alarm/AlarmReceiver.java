package hust.nursenfcclient.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import de.greenrobot.event.EventBus;

/**
 * Created by admin on 2015/12/5.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final int ALARM_TYPE_TIMEIN = 1; // 提醒

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        switch (data.getInt(AlarmEvent.ALARM_TYPE)) {
            case ALARM_TYPE_TIMEIN:
                EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_ALARM_TIME_IN, data.getString(AlarmEvent.ALARM_DATA)));
                break;
        }
    }


}
