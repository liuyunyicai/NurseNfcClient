package hust.nursenfcclient.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by admin on 2015/12/4.
 */
public class UploadDbHelper {

    private SQLiteDatabase db;
    private NurseNFCDatabaseHelper nurseDbHelper;

    public static UploadDbHelper instance = null;

    private UploadDbHelper(Context context) {
        nurseDbHelper = NurseNFCDatabaseHelper.getInstance(context);
    }

    public static UploadDbHelper getInstance(Context context) {
        if (instance == null)
            instance = new UploadDbHelper(context);
        return instance;
    }

    // ==== 上传数据 === //
    public JSONObject getUpLoadData() {
        JSONObject resultObject = new JSONObject();
        nurseDbHelper.getDataFromDbToUpLoad(resultObject);
        Log.w("LOG_TAG", "UploadDbHelper 获取到数据库中的数据" + resultObject.toString());
        return resultObject;
    }
}
