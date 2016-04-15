package hust.nursenfcclient.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import hust.nursenfcclient.network.ServicesHelper;

/**
 * Created by admin on 2015/11/25.
 */

// 数据库操作辅助类
public class NurseNFCDatabaseHelper implements ServicesHelper {
    private final static String DB_LOGTAG = "DB_LOGTAG";

    // 使用SQliteOpenHelper来帮助实现创建、打开和升级数据库
    private static class NurseDbOpenHelper extends SQLiteOpenHelper {
        public static final String DATABASE_NAME = "nursenfc_db";// 数据库名
        private static final int DATABASE_VERSION = 1;              // 数据库版本

        // 数据库创建语句
        private final static String CREATE_NURSE_INFO_SQL =
                "CREATE TABLE nurse_info(\n" +
                        "nurse_id TEXT PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "nurse_name TEXT NOT NULL,\n" +
                        "nurse_photo TEXT\n" +
                        ");";
        private final static String CREATE_HOUSE_INFO_SQL =
                "CREATE TABLE house_info(\n" +
                        "house_id TEXT PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "nurse_id TEXT NOT NULL,\n" +
                        "house_state CHECK(house_state IN ('checked','uncheck')) NOT NULL,\n" +
                        "FOREIGN KEY (nurse_id) REFERENCES nurse_info(nurse_id) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                        ");";
        private final static String CREATE_BED_INFO_SQL =
                "CREATE TABLE bed_info(\n" +
                        "bed_id TEXT PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "house_id TEXT NOT NULL,\n" +
                        "patient_id TEXT UNIQUE,\n" +
                        "bed_state CHECK(bed_state IN ('empty','checked','uncheck')) NOT NULL,\n" +
                        "FOREIGN KEY (house_id) REFERENCES house_info(house_id) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
                        "FOREIGN KEY (patient_id) REFERENCES patient_info(patient_id) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                        ");";
        private final static String CREATE_PATIENT_INFO_SQL =
                "CREATE TABLE patient_info(\n" +
                        "patient_id TEXT PRIMARY KEY UNIQUE NOT NULL,\n" +
                        "tag_id TEXT UNIQUE,\n" +
                        "patient_name TEXT NOT NULL,\n" +
                        "patient_age INTEGER NOT NULL,\n" +
                        "patient_gender CHECK(patient_gender IN ('male','female')) NOT NULL,\n" +
                        "patient_record TEXT NOT NULL,\n" +
                        "patient_photo TEXT\n" +
                        ");";
        private final static String CREATE_TEMPERATURE_INFO_SQL =
                "CREATE TABLE temperature_info(\n" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE NOT NULL,\n" +
                        "tag_id TEXT NOT NULL,\n" +
                        "temper_num REAL NOT NULL,\n" +
                        "nurse_id TEXT NOT NULL,\n" +
                        "last_time TEXT NOT NULL,\n" +
                        "next_time TEXT,\n" +
                        "FOREIGN KEY (nurse_id) REFERENCES nurse_info(nurse_id) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                        ");";


        // 构造函数
        public NurseDbOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 创建数据库中的表
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_NURSE_INFO_SQL);
            db.execSQL(CREATE_HOUSE_INFO_SQL);
            db.execSQL(CREATE_BED_INFO_SQL);
            db.execSQL(CREATE_PATIENT_INFO_SQL);
            db.execSQL(CREATE_TEMPERATURE_INFO_SQL);
            Log.i("LOG_TAG", "数据库创建成功");
        }

        // 数据库版本更新
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("LOG_TAG", "DataBase Change From Version " + oldVersion + " To " + newVersion);
        }
    }


    private final String[] table_names = {NURSE_INFO_TABLE_NAME, HOUSE_INFO_TABLE_NAME, BED_INFO_TABLE_NAME,
            PATIENT_INFO_TABLE_NAME, TEMPER_INFO_TABLE_NAME};

    // SQliteOpenHelper实例
    private NurseDbOpenHelper openHelper;
    private static NurseNFCDatabaseHelper instance = null;
    private Context mContext;
    private SQLiteDatabase db;

    // 构造函数
    private NurseNFCDatabaseHelper(Context context) {
        mContext = context;
        openHelper = new NurseDbOpenHelper(context, NurseDbOpenHelper.DATABASE_NAME,
                null, NurseDbOpenHelper.DATABASE_VERSION);
    }

    // 单例模式
    public static NurseNFCDatabaseHelper getInstance(Context context) {
        if (instance == null)
            instance = new NurseNFCDatabaseHelper(context);
        return instance;
    }

    public SQLiteDatabase getDbConnection() {
        return openHelper.getWritableDatabase();
    }

    // 关闭数据库
    public void closeDatabase() {
        if (openHelper != null)
            openHelper.close();
    }

    private final String INSERT_SQL = "INSERT INTO table_name VALUES(";
    private final String REPLACE_STR = "table_name";
    private final String DOT = ",";
    private final String CIRCLE = ")";

    // ===== 将下载的数据填入到数据库中 ==== //
    public boolean initDataFromServer(byte[] data_bytes) {
        boolean isSuccess = false;

        try {
            // ==== 解析结果字符串 ===== //
            String dataStr = new String(data_bytes, Charset.forName("UTF-8"));
            JSONObject jsonObject = new JSONObject(dataStr);

            // 解析数据信息，并向数据库插入数据
            getAndInsertAllDataIntoDb(NURSE_INFO_TABLE_NAME, jsonObject);
            getAndInsertAllDataIntoDb(HOUSE_INFO_TABLE_NAME, jsonObject);
            getAndInsertAllDataIntoDb(BED_INFO_TABLE_NAME, jsonObject);
            getAndInsertAllDataIntoDb(PATIENT_INFO_TABLE_NAME, jsonObject);
            getAndInsertAllDataIntoDb(TEMPER_INFO_TABLE_NAME, jsonObject);

        } catch (JSONException e) {
            Log.e("LOG_TAG", e.toString());
        } catch (Exception e) {
            Log.e("LOG_TAG", e.toString());
        }
        return isSuccess;
    }

    // ======= 从JSONArray中获取数据并插入到数据库中 ====== //
    private void getAndInsertAllDataIntoDb(String table_name, JSONObject jsonObject) throws JSONException {
        insertTableValueIntoDb(table_name, jsonObject.getJSONArray(table_name));
    }

    // 从JSONArray中获取信息
    private String[][] getValuesFromJSON(JSONArray jsonArray, ArrayList<String[]> params_lists) throws JSONException {
        String[][] mValues = new String[jsonArray.length()][params_lists.size()];

        for (int i = 0; i < jsonArray.length(); i++) {
            mValues[i] = getValueFromJSONObject(jsonArray.getJSONObject(i), params_lists);
        }
        return mValues;
    }

    // 从JSONOObject获取信息
    private String[] getValueFromJSONObject(JSONObject jsonObject, ArrayList<String[]> params_lists) throws JSONException {
        String[] resultValues = new String[params_lists.size()];
        DecimalFormat decimalFormat=new DecimalFormat(".0000");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        for (int i = 0; i < params_lists.size(); i++) {
            if (!jsonObject.has(params_lists.get(i)[1])) {
                resultValues[i] = null;
            } else {
                switch (params_lists.get(i)[0]) {
                    case TYPE_TEXT:
                    case TYPE_STRING:
                    case TYPE_ENUM:
                    case TYPE_TIME:
                        resultValues[i] = jsonObject.getString(params_lists.get(i)[1]);
                        break;
                    case TYPE_INTEGER:
                        resultValues[i] = String.valueOf(jsonObject.getInt(params_lists.get(i)[1]));
                        break;
                    case TYPE_FLOAT:
                        resultValues[i] = decimalFormat.format(jsonObject.getDouble(params_lists.get(i)[1]));
                        break;
                }
            }
        }
        return resultValues;
    }

    //==============================================================================================================================//
    // ====== 将数据全部插入到数据库中 ===== //
    private boolean insertTableValueIntoDb(String table_name, JSONArray jsonArray) throws JSONException {
        return getAndInsertValueIntoDb(table_name, jsonArray, getParamsListsFromTableName(table_name));
    }
//
//    private boolean insertNurseValueIntoDb(JSONArray jsonArray) throws JSONException {
//        return getAndInsertValueIntoDb(NURSE_INFO_TABLE_NAME, jsonArray, getParamsListsFromTableName(NURSE_INFO_TABLE_NAME));
//    }
//
//    private boolean insertHouseValueIntoDb(JSONArray jsonArray) throws JSONException {
//        return getAndInsertValueIntoDb(HOUSE_INFO_TABLE_NAME, jsonArray, getParamsListsFromTableName(HOUSE_INFO_TABLE_NAME));
//    }
//
//    private boolean insertBedValueIntoDb(JSONArray jsonArray) throws JSONException {
//        return getAndInsertValueIntoDb(BED_INFO_TABLE_NAME, jsonArray, getParamsListsFromTableName(BED_INFO_TABLE_NAME));
//    }
//
//    private boolean insertPatientValueIntoDb(JSONArray jsonArray) throws JSONException {
//        return getAndInsertValueIntoDb(PATIENT_INFO_TABLE_NAME, jsonArray, getParamsListsFromTableName(PATIENT_INFO_TABLE_NAME));
//    }
//
//    private boolean insertTemperValueIntoDb(JSONArray jsonArray) throws JSONException {
//        return getAndInsertValueIntoDb(TEMPER_INFO_TABLE_NAME, jsonArray, getParamsListsFromTableName(TEMPER_INFO_TABLE_NAME));
//    }

    // ===== 获得每一个TABLE表中的字段名及字段类型 ===== //
    private ArrayList<String[]> getParamsListsFromTableName(String table_name) {
        ArrayList<String[]> params_lists = new ArrayList<>();

        switch (table_name) {
            case NURSE_INFO_TABLE_NAME: {
                params_lists.add(new String[]{TYPE_STRING, NURSE_NAME});
                params_lists.add(new String[]{TYPE_STRING, NURSE_ID});
                params_lists.add(new String[]{TYPE_STRING, NURSE_PHOTO});
            }
            break;
            case HOUSE_INFO_TABLE_NAME: {
                params_lists.add(new String[]{TYPE_STRING, HOUSE_ID});
                params_lists.add(new String[]{TYPE_STRING, NURSE_ID});
                params_lists.add(new String[]{TYPE_ENUM, HOUSE_STATE});
            }
            break;
            case BED_INFO_TABLE_NAME: {
                params_lists.add(new String[]{TYPE_STRING, BED_ID});
                params_lists.add(new String[]{TYPE_STRING, HOUSE_ID});
                params_lists.add(new String[]{TYPE_STRING, PATIENT_ID});
                params_lists.add(new String[]{TYPE_ENUM, BED_STATE});
            }
            break;
            case PATIENT_INFO_TABLE_NAME: {
                params_lists.add(new String[]{TYPE_STRING, PATIENT_ID});
                params_lists.add(new String[]{TYPE_STRING, TAG_ID});
                params_lists.add(new String[]{TYPE_STRING, PATIENT_NAME});
                params_lists.add(new String[]{TYPE_INTEGER, PATIENT_AGE});
                params_lists.add(new String[]{TYPE_ENUM, PATIENT_GENDER});
                params_lists.add(new String[]{TYPE_TEXT, PATIENT_RECORD});
                params_lists.add(new String[]{TYPE_TEXT, PATIENT_PHOTO});
            }
            break;
            case TEMPER_INFO_TABLE_NAME: {
                params_lists.add(new String[]{TYPE_STRING, TAG_ID});
                params_lists.add(new String[]{TYPE_FLOAT, TEMPER_NUM});
                params_lists.add(new String[]{TYPE_STRING, NURSE_ID});
                params_lists.add(new String[]{TYPE_TIME, LAST_TIME});
                params_lists.add(new String[]{TYPE_TIME, NEXT_TIME});
            }
            break;
        }
        return params_lists;
    }

    // 从JSON中获取到数据，封装到values中，每一行代表一条记录，然后插入到数据库中
    private boolean getAndInsertValueIntoDb(String table_name, JSONArray jsonArray, ArrayList<String[]> params_lists)
            throws JSONException {
        String[][] values = getValuesFromJSON(jsonArray, params_lists);
        return insertAllIntoTable(table_name, values, params_lists.size(), params_lists);
    }


    // ====== 一系列操作数据库函数 ====== //
    // 通用的Insert操作

    /**
     * @params table_name:需要添加数据的table
     * values：对应的每一个字段的具体值
     * params_type: 每一个参数的数据类型
     * params_num:values默认是可以有多组数据的，因此需要num进行必要的分段
     */
    public boolean insertAllIntoTable(String table_name, String[][] values, int params_num, ArrayList<String[]> params_lists) {
        // 判断是否合法
        if (!isValidInsertRequest(table_name, values, params_num))
            return false;

        boolean result = false;
        try {
            db = openHelper.getWritableDatabase();
            // 同时插入多条记录
            for (int i = 0; i < values.length; i++) {
                // 组装插入字符串
                ContentValues newValues = new ContentValues();
                for (int j = 0; j < values[i].length; j++) {
                    if (values[i][j] != null)
                        putNewValues(newValues, params_lists.get(j), values[i][j]);
                }
                // 插入数据：
                db.insert(table_name, null, newValues);
            }
            result = true;
        } catch (SQLiteConstraintException e) {
            Log.e(DB_LOGTAG, e.toString());
        } catch (SQLException e) {
            Log.e(DB_LOGTAG, e.toString());
        } finally {
            db.close();
        }
        return result;
    }

    // 插入数据
    private ContentValues putNewValues(final ContentValues newValues, String[] params_list, String value) {
        switch (params_list[0]) {
            case TYPE_TEXT:
            case TYPE_STRING:
            case TYPE_ENUM:
            case TYPE_TIME:
                newValues.put(params_list[1], value);
                break;
            case TYPE_INTEGER:
                newValues.put(params_list[1], Integer.valueOf(value));
                break;
            case TYPE_FLOAT:
                newValues.put(params_list[1], Double.valueOf(value));
                break;
        }
        return newValues;
    }

    // 判断param类型是否为String
    private boolean isText(String param_type) {
        switch (param_type) {
            case TYPE_TEXT:
            case TYPE_STRING:
            case TYPE_ENUM:
            case TYPE_TIME:
                return true;
        }
        return false;
    }

    // 判断发送来的数据是否合法
    private boolean isValidInsertRequest(String table_name, String[][] values, int params_num) {
        boolean isValid = false;

        for (String valid_table_name : table_names) {
            isValid |= table_name.equals(valid_table_name);
        }
        return isValid && ((values != null) && (values.length != 0))
                && ((params_num > 0) && (values[0].length % params_num == 0));
    }


    // ===== 操作单个TABLE的函数 ======= //
    public boolean insertIntoNurseTable(String nurse_id, String nurse_name, @Nullable String nurse_photo) {
        boolean result = false;

        String sql = INSERT_SQL.replace(REPLACE_STR, NURSE_INFO_TABLE_NAME);
        sql += nurse_id + DOT + nurse_name + DOT + (nurse_photo == null ? "null" : nurse_photo) + ")";

        try {
            db = openHelper.getWritableDatabase();
            db.execSQL(sql);
            result = true;
        } catch (SQLException e) {
            Log.i(DB_LOGTAG, e.toString());
        }
        return result;
    }

    //===========================================================================================================================//
    // ****************** 查询数据库相关函数 **************** //

    // ====== 从数据库中查询数据 ====== //
    private Cursor queryDataFromDb(String table_name, ArrayList<String[]> params_lists) {
        String[] result_columns = new String[params_lists.size()];

        for (int i = 0; i < params_lists.size(); i++) {
            result_columns[i] = params_lists.get(i)[1];
        }

        // 查询的相关参数
        // Where条件语句
        String where = null;
        String whereArgs[] = null;
        String groupBy = null;
        String having = null;
        String order = null;

        // 根据SQLiteOpenHelper获取DataBase实例
        SQLiteDatabase db = openHelper.getWritableDatabase();
        /** 根据语句查询数据库*/
        Cursor cursor = db.query(table_name, result_columns, where, whereArgs, groupBy, having, order);
        // 返回结果Cursor
        return cursor;
    }

    // 解析获得的数据
    private JSONArray pharseDataFromCursor(String table_name, ArrayList<String[]> params_lists) throws JSONException {
        Cursor cursor = queryDataFromDb(table_name, params_lists);
        JSONArray jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
            JSONObject jsonObject = new JSONObject();

            // 解析字符串
            for (int i = 0; i < params_lists.size(); i++) {
                String[] params_list = params_lists.get(i);
                int index = cursor.getColumnIndexOrThrow(params_list[1]);
                switch (params_list[0]) {
                    case TYPE_TEXT:
                    case TYPE_STRING:
                    case TYPE_ENUM:
                    case TYPE_TIME:
                        jsonObject.put(params_list[1], cursor.getString(index));
                        break;
                    case TYPE_INTEGER:
                        jsonObject.put(params_list[1], cursor.getInt(index));
                        break;
                    case TYPE_FLOAT:
                        jsonObject.put(params_list[1], cursor.getDouble(index));
                        break;
                }
            }
            jsonArray.put(jsonObject);
        }

        return jsonArray;
    }

    // ==== 将获得的数据组装为JSONObject ===== //
    public JSONObject getAllDataFromTable(JSONObject resultJsonObj, String table_name) throws JSONException {
        return resultJsonObj.put(table_name, pharseDataFromCursor(table_name, getParamsListsFromTableName(table_name)));
    }

    /********************************************************************************************************/
    // ==== 查询数据库中所有数据上传服务器 ====== //
    public int getDataFromDbToUpLoad(StringBuffer result_data_buffer) {
        final String METHOD_TAG = "getDataFromDbToUpLoad";
        int isSuccess = ACK_FAILURE;
        JSONObject resultJsonObj = new JSONObject();
        try {
            getAllDataFromTable(resultJsonObj, NURSE_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, HOUSE_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, BED_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, PATIENT_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, TEMPER_INFO_TABLE_NAME);

            isSuccess = ACK_SUCCESS;
            result_data_buffer.append(resultJsonObj.toString());

        } catch (SQLException e) {
            onCatchSQLException(METHOD_TAG, e);
        } catch (Exception e) {
            onCatchException(METHOD_TAG, e);
        }

        return isSuccess;
    }

    public int getDataFromDbToUpLoad(JSONObject resultJsonObj) {
        final String METHOD_TAG = "getDataFromDbToUpLoad";
        int isSuccess = ACK_FAILURE;
        try {
            getAllDataFromTable(resultJsonObj, NURSE_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, HOUSE_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, BED_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, PATIENT_INFO_TABLE_NAME);

            getAllDataFromTable(resultJsonObj, TEMPER_INFO_TABLE_NAME);

            isSuccess = ACK_SUCCESS;

        } catch (SQLException e) {
            onCatchSQLException(METHOD_TAG, e);
        } catch (Exception e) {
            onCatchException(METHOD_TAG, e);
        }
        return isSuccess;
    }

    // ****** 对SQLException事件的处理函数  **********//
    private void onCatchSQLException(String METHOD_TAG, SQLException e) {
        Log.e("LOG_TAG", METHOD_TAG + "数据库操作出错，错误代码：" + e.toString());
    }

    // ****** 对Exception事件的处理函数 ********//
    private void onCatchException(String METHOD_TAG, Exception e) {
        Log.e("LOG_TAG", METHOD_TAG + "发现未知错误，错误代码：" + e.toString());
    }

    // ====== 清空表中所有数据 ===== //
    public void clearAllTableDatas() {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        String[] table_names = {NURSE_INFO_TABLE_NAME, HOUSE_INFO_TABLE_NAME, BED_INFO_TABLE_NAME,
                PATIENT_INFO_TABLE_NAME, TEMPER_INFO_TABLE_NAME};

        for (int i = 0; i < table_names.length; i++) {
            String sql = "DELETE FROM " + table_names[i];
            db.execSQL(sql);
        }
        Log.i("LOG_TAG", "数据库清空完毕");
    }

    // ===== 图片路径相关处理函数 ==== //
    public List<String> getPhotosUri() {
        List<String> uris = new ArrayList<>();
        uris.add(getNursePhotoUri());
        uris.addAll(getPatientPhotosUri());
        return uris;
    }


    public String getNursePhotoUri() {
        Cursor cursor = queryDataFromDb(NURSE_INFO_TABLE_NAME, getParamsListsFromTableName(NURSE_INFO_TABLE_NAME));
        String photoUri = "";
        while (cursor.moveToNext()) {
            photoUri = cursor.getString(cursor.getColumnIndexOrThrow(NURSE_PHOTO));
        }
        return photoUri;
    }

    public List<String> getPatientPhotosUri() {
        Cursor cursor = queryDataFromDb(PATIENT_INFO_TABLE_NAME, getParamsListsFromTableName(PATIENT_INFO_TABLE_NAME));
        List<String> uris = new ArrayList<>();
        int index = cursor.getColumnIndexOrThrow(PATIENT_PHOTO);
        while (cursor.moveToNext()) {
            uris.add(cursor.getString(index));
        }
        return uris;
    }

}
