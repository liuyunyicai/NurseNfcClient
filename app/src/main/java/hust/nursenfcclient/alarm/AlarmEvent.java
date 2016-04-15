package hust.nursenfcclient.alarm;

/**
 * Created by admin on 2015/12/5.
 */
public class AlarmEvent {

    public static final String ALARM_ACTION = "action.alarm";
    public static final String ALARM_DATA = "alarm_data";
    public static final String ALARM_TYPE = "alarm_type";

    public static final int ACTION_START_ALARM = 1;   // 打开Alarm
    public static final int ACTION_RESTART_ALARM = 2; // 重启Alarm
    public static final int ACTION_CANCEL_ALARM = 3;  // 关闭Alarm
    public static final int ACTION_ALARM_TIME_IN = 4;  // Alarm提醒时间到

    public static final int ACTION_STRAT_UPDATE_DB = 5; // 开启数据库维护线程
    public static final int ACTION_CLOSE_UPDATE_DB = 6; // 关闭数据库维护线程

    private int action;
    private String data;


    public AlarmEvent(int action) {
        this.action = action;
    }

    public AlarmEvent(int action, String data) {
        this.action = action;
        this.data = data;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
