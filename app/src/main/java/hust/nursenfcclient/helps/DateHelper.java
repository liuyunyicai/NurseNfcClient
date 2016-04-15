package hust.nursenfcclient.helps;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import hust.nursenfcclient.MainActivity;

/**
 * Created by admin on 2015/12/19.
 */
public class DateHelper {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 获取系统当前时间
    public static String getCurTime() {
        return SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis()));
    }

    // 从事件字符串中获取小时
    public static int getHourFromTimeStr(String timeStr) {
        return Integer.valueOf(timeStr.substring(11, 13));
    }

    // 从时间字符串中获取分钟
    public static int getMinuteFromTimeStr (String timeStr) {
        return Integer.valueOf(timeStr.substring(14, 16));
    }

    // 从时间字符串中获取秒
    public static int getSecondFromTimeStr(String timeStr) {
        return Integer.valueOf(timeStr.substring(17, 19));
    }

    // 获取两个时间字符串的时间间隔（单位ms）
    public static int getMsInterval(String curStr, String nextStr) {
        int curHour = getHourFromTimeStr(curStr);
        int curMin = getMinuteFromTimeStr(curStr);
        int curSec = getSecondFromTimeStr(curStr);

        int nextHour = getHourFromTimeStr(nextStr);
        int nextMin = getMinuteFromTimeStr(nextStr);
        int nextSec = getSecondFromTimeStr(nextStr);

        int timeMs = ((nextHour - curHour) * 60 + (nextMin - curMin)) * 60 + (nextSec - curSec); //单位秒钟
        Log.d(MainActivity.LOG_TAG, "timeMs ==" + timeMs);
        return timeMs;
    }
}
