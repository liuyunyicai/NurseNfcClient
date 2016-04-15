package hust.nursenfcclient.database;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用来维护数据库中当病人下一次测量温度next_tiem到来时，bed_state与house_state的更新
 *
 * Created by admin on 2015/12/5.
 */
public class UpdateDbStateThread implements Runnable {
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int SLEEP_TIME = 60 * 1000;

    private Context mContext;
    private static boolean isRunning = false;

    public UpdateDbStateThread(Context context) {
        mContext = context;
        isRunning = true;
    }

    @Override
    public void run() {
        Log.i("LOG_TAG", "UpdateDbStateThread线程启动");
        try {
            while (isRunning) {
                Thread.sleep(SLEEP_TIME);
                // 开始更新数据库
                DatabaseQueryHelper.getInstance(mContext).getNeedUpdateInfoFromDb();
            }

        } catch (InterruptedException e) {
            Log.e("LOG_TAG", "UpdateDbStateThread : " + e.toString());
        }
        Log.i("LOG_TAG", "UpdateDbStateThread线程已退出");
    }

    public static void closeUpdateDbThread() {
        isRunning = false;
    }

    public static boolean isUpdateDbThreadRunning() {
        return isRunning;
    }

}
