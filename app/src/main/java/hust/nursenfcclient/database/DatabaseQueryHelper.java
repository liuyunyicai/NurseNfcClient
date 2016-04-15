package hust.nursenfcclient.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import hust.nursenfcclient.house.HouseInfoItem;
import hust.nursenfcclient.network.ServicesHelper;
import hust.nursenfcclient.patient.PatientInfoItem;
import hust.nursenfcclient.patient.TemperInfoItem;

/**
 * Created by admin on 2015/11/30.
 */
public class DatabaseQueryHelper implements ServicesHelper {

    private SQLiteDatabase db;
    private NurseNFCDatabaseHelper nurseDbHelper;

    public static DatabaseQueryHelper instance = null;

    private DatabaseQueryHelper(Context context) {
        nurseDbHelper = NurseNFCDatabaseHelper.getInstance(context);
    }

    public static DatabaseQueryHelper getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseQueryHelper(context);
        return instance;
    }

    // === 查询有多少病房，并且多少病房未check ==== //
    public int[] getRemainTaskFromDb() {
        int[] bed_count = new int[4];

        String houseAllSql = "SELECT COUNT(*) FROM " + HOUSE_INFO_TABLE_NAME;
        String houseRemainSql = "SELECT COUNT(*) FROM " + HOUSE_INFO_TABLE_NAME + " WHERE " + HOUSE_STATE + "='uncheck'";
        String bedAllSql = "SELECT COUNT(*) FROM " + BED_INFO_TABLE_NAME;
        String bedRemainSql = "SELECT COUNT(*) FROM " + BED_INFO_TABLE_NAME + " WHERE " + BED_STATE + "='uncheck'";

        String[] sqls = {houseAllSql, houseRemainSql, bedAllSql, bedRemainSql};
        Cursor cursor = null;
        try {
            db = nurseDbHelper.getDbConnection();

            for (int i = 0; i < bed_count.length; i++) {
                bed_count[i] = queryCount(cursor, sqls[i]);
//                Log.i("LOG_TAG",String.valueOf(bed_count[i]));
            }
        } catch (Exception e) {
            Log.i("LOG_TAG", "查询Task信息出现问题：" + e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return bed_count;
    }

    private int queryCount(Cursor cursor, String sql) {
        int count = 0;
        cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        return count;
    }

    // ===== 从数据库中查询HOUSE的相关信息 ==== //
    public List<HouseInfoItem> getHouseInfoFromDb() {
        List<HouseInfoItem> data_lists = new ArrayList<>();

        // 查询HOUSEINFO表查询所有house_id
        String sql = "SELECT * FROM " + HOUSE_INFO_TABLE_NAME;

        Cursor cursor = null;
        try {
            db = nurseDbHelper.getDbConnection();
            // 查询房间的基本信息
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                HouseInfoItem item = new HouseInfoItem();
                item.setHouseId(cursor.getString(cursor.getColumnIndexOrThrow(HOUSE_ID)));
                item.setHouseState(cursor.getString(cursor.getColumnIndexOrThrow(HOUSE_STATE)));
                data_lists.add(item);
            }

            // 根据房间houseId分别进行查询
            for (HouseInfoItem item : data_lists) {
                String houseId = item.getHouseId();
                int bed_all_count = 0;
                int bed_check_count = 0;

                String bed_sql = "SELECT * FROM " + BED_INFO_TABLE_NAME + " WHERE " + HOUSE_ID + "='" + houseId + "'";
                cursor = db.rawQuery(bed_sql, null);

                List<String> bed_ids = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String bed_state = cursor.getString(cursor.getColumnIndexOrThrow(BED_STATE));

                    if ((bed_state.equals(HouseInfoItem.CHECKED_STR)) || (bed_state.equals(HouseInfoItem.UNCHECK_STR))) {
                        bed_ids.add(cursor.getString(cursor.getColumnIndexOrThrow(BED_ID)));
                        ++bed_all_count;
                        if (bed_state.equals(HouseInfoItem.CHECKED_STR))
                            ++bed_check_count;
                    }
                }
                item.setBed_ids(bed_ids);
                item.setBedAllNum(bed_all_count);
                item.setBedCheckedNum(bed_check_count);
                item.resetCheckedPrecent();
            }

        } catch (Exception e) {
            Log.i("LOG_TAG", "查询Task信息出现问题：" + e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return data_lists;
    }

    // ===== 从数据库中查询Patient的相关信息 ====//
    public List<PatientInfoItem> getPatientInfoFromDb(String[] bedIds) {
        List<PatientInfoItem> lists = new ArrayList<>();
        if (bedIds != null) {
            // 通过bed_id查询bed_state，及其对应的patient_id
            String sql = "";
            Cursor cursor = null;

            try {
                db = nurseDbHelper.getDbConnection();
                for (String bed_id : bedIds) {
                    sql = "SELECT * FROM " + BED_INFO_TABLE_NAME + " WHERE " + BED_ID + "='" + bed_id + "'";
                    // 查询房间的基本信息
                    cursor = db.rawQuery(sql, null);
                    PatientInfoItem item = new PatientInfoItem();
                    while (cursor.moveToNext()) {
                        item.setBedId(bed_id);
                        item.setBedState(cursor.getString(cursor.getColumnIndexOrThrow(BED_STATE)));
                        item.setPatientId(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_ID)));
                    }

                    // 根据patient_id查询patient的相关信息
                    sql = "SELECT * FROM " + PATIENT_INFO_TABLE_NAME + " WHERE " + PATIENT_ID + "='" + item.getPatientId() + "'";
                    cursor = db.rawQuery(sql, null);
                    while (cursor.moveToNext()) {
                        item.setPatientAge(Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_AGE))));
                        item.setPatientGender(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_GENDER)));
                        item.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME)));
                        item.setPatientRecord(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_RECORD)));
                        item.setPatientPhoto(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_PHOTO)));
                        item.setTag_id(cursor.getString(cursor.getColumnIndexOrThrow(TAG_ID)));
                    }

                    // 根据TAG_ID查询最后一次测量温度
                    sql = "SELECT * FROM " + TEMPER_INFO_TABLE_NAME + " WHERE " + TAG_ID + "='" + item.getTag_id()
                            + "' ORDER BY " + ID + " DESC LIMIT 1";
                    cursor = db.rawQuery(sql, null);
                    while (cursor.moveToNext()) {
                        item.setLastTemper(Float.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(TEMPER_NUM))));
                    }
                    lists.add(item);
                }
            } catch (Exception e) {
                Log.i("LOG_TAG", "查询Patient信息出现问题：" + e.toString());
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        return lists;
    }

    // ==== 获取Temper测量记录 ==== //
    public List<TemperInfoItem> getTemperInfoFromDb(String tag_id) {
        List<TemperInfoItem> lists = new ArrayList<>();
        // 通过bed_id查询bed_state，及其对应的patient_id
        String sql = "SELECT * FROM " + TEMPER_INFO_TABLE_NAME + " WHERE " + TAG_ID + "='" + tag_id + "'";
        Cursor cursor = null;
        try {
            db = nurseDbHelper.getDbConnection();
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                TemperInfoItem item = new TemperInfoItem();
                item.setTag_id(tag_id);
                item.setNurse_id(cursor.getString(cursor.getColumnIndexOrThrow(NURSE_ID)));
                item.setTemper_num(Float.valueOf(cursor.getFloat(cursor.getColumnIndexOrThrow(TEMPER_NUM))));
                item.setLast_time(cursor.getString(cursor.getColumnIndexOrThrow(LAST_TIME)));
                if (!cursor.isNull(cursor.getColumnIndexOrThrow(NEXT_TIME)))
                    item.setNext_time(cursor.getString(cursor.getColumnIndexOrThrow(NEXT_TIME)));

                lists.add(item);
            }
        } catch (Exception e) {
            Log.i("LOG_TAG", "查询Patient信息出现问题：" + e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return lists;
    }

    public static final int GET_DB_EXCEPTION = 0;
    public static final int TAG_PAIRED = 1;
    public static final int TAG_UNPAIR = -1;
    // 查询PatientInfo判断此tag_id是否已经与病人配对
    public int getIsTagPairedFromDb(String tag_id) {
        int isPaired = GET_DB_EXCEPTION;
        if (tag_id == null || tag_id.length() == 0)
            return isPaired;

        Cursor cursor = null;
        try {
            String sql = "SELECT * FROM " + PATIENT_INFO_TABLE_NAME + " WHERE " + TAG_ID + "='" + tag_id + "'";
            db = nurseDbHelper.getDbConnection();
            // 查询房间的基本信息
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                if (!cursor.isNull(cursor.getColumnIndexOrThrow(PATIENT_ID)))
                    isPaired = TAG_PAIRED;
            } else {
                isPaired = TAG_UNPAIR;
            }
        } catch (Exception e) {
            Log.i("LOG_TAG", "getIsTagPairedFromDb信息出现问题：" + e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return isPaired;
    }

    // 查询所有未匹配病人信息
    public List<PatientInfoItem> getUnpairPatientFromDb() {
        List<PatientInfoItem> lists = new ArrayList<>();

        String sql = "";
        Cursor cursor = null;
        try {
            // 先查询所有tag_id未null的病人patient_id
            sql = "SELECT * FROM " + PATIENT_INFO_TABLE_NAME;
            db = nurseDbHelper.getDbConnection();
            // 查询房间的基本信息
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                if (cursor.isNull(cursor.getColumnIndexOrThrow(TAG_ID))) {
                    PatientInfoItem item = new PatientInfoItem();
                    item.setPatientId(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_ID)));
                    item.setPatientAge(Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_AGE))));
                    item.setPatientGender(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_GENDER)));
                    item.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME)));
                    item.setPatientRecord(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_RECORD)));
                    item.setPatientPhoto(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_PHOTO)));
                    lists.add(item);
                }
            }

            // === 查询对应的Bed_id =====//
            for (int i = 0; i < lists.size(); i++) {
                PatientInfoItem item = lists.get(i);
                String patient_id = item.getPatientId();

                sql = "SELECT * FROM " + BED_INFO_TABLE_NAME + " WHERE " + PATIENT_ID + "='" + patient_id + "'";
                cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    item.setBedId(cursor.getString(cursor.getColumnIndexOrThrow(BED_ID)));
                    item.setBedState(cursor.getString(cursor.getColumnIndexOrThrow(BED_STATE)));
                    item.setHouseId(cursor.getString(cursor.getColumnIndexOrThrow(HOUSE_ID)));
                }
            }
        } catch (Exception e) {
            Log.i("LOG_TAG", "查询Patient信息出现问题：" + e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return lists;

    }

    // ==== patient_id与tag_id进行配对 ==== //
    public boolean pairPatientAndTag(String patient_id, TemperInfoItem item) {
        String tag_id = item.getTag_id();
        if (patient_id == null || tag_id == null)
            return false;
        boolean isSuccess = false;
        String sql = "UPDATE " + PATIENT_INFO_TABLE_NAME + " SET " + TAG_ID + "='" + tag_id + "' WHERE " +
                PATIENT_ID + "='" + patient_id + "'";

        try {
            // 先刷新patient_info，更新原来为null值的tag_id
            db = nurseDbHelper.getDbConnection();
            db.execSQL(sql);

            isSuccess = true;
        } catch (Exception e) {
            Log.i("LOG_TAG", "pairPatientAndTag出现问题：" + e.toString());
        }

        return isSuccess;
    }

    // 向TemperInfo中插入一条测量记录
    public void insertIntoTemperInfo(TemperInfoItem item) {
        // 向temper_info中插入一条测量记录
        ContentValues newValues = new ContentValues();
        newValues.put(TAG_ID, item.getTag_id());
        newValues.put(TEMPER_NUM, item.getTemper_num());
        newValues.put(NURSE_ID, item.getNurse_id());

        newValues.put(LAST_TIME, item.getLast_time());
        newValues.put(NEXT_TIME, item.getNext_time());

        db.insert(TEMPER_INFO_TABLE_NAME, null, newValues);
    }

    // ===== 根据TAG_ID获取patient_info ===== //
    public PatientInfoItem getPatientInfoUseTagId(String tag_id) {
        PatientInfoItem item = new PatientInfoItem();
        Cursor cursor = null;
        try {
            String sql = "SELECT * FROM " + PATIENT_INFO_TABLE_NAME + " WHERE " + TAG_ID + "='" + tag_id + "'";

            db = nurseDbHelper.getDbConnection();
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                item.setPatientId(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_ID)));
                item.setPatientAge(Integer.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_AGE))));
                item.setPatientGender(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_GENDER)));
                item.setPatientName(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_NAME)));
                item.setPatientRecord(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_RECORD)));
                item.setPatientPhoto(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_PHOTO)));
                item.setTag_id(cursor.getString(cursor.getColumnIndexOrThrow(TAG_ID)));
            }

            // 同时注意更新patient_id状态为已测量
            sql = "UPDATE " + BED_INFO_TABLE_NAME + " SET " + BED_STATE + "='checked'" +
                    " WHERE " + PATIENT_ID + "='" + item.getPatientId() + "'";
            db.execSQL(sql);

            // 查询BED_ID
            sql = "SELECT * FROM " + BED_INFO_TABLE_NAME + " WHERE " + PATIENT_ID + "='" + item.getPatientId() + "'";
            cursor = db.rawQuery(sql, null);
            String house_id = "";
            while (cursor.moveToNext()) {
                item.setBedId(cursor.getString(cursor.getColumnIndexOrThrow(BED_ID)));
                item.setBedState(cursor.getString(cursor.getColumnIndexOrThrow(BED_STATE)));

                house_id = cursor.getString(cursor.getColumnIndexOrThrow(HOUSE_ID));
            }

            // 判断该房间的是否已经全部checked，如果是，则更新HOUSE_STATE
            sql = "SELECT * FROM " + BED_INFO_TABLE_NAME + " WHERE " + HOUSE_ID + "='" + house_id + "'";
            cursor = db.rawQuery(sql, null);
            String house_state = "checked";
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow(BED_STATE)).equals("uncheck")) {
                    house_state = "uncheck";
                }
            }

            // 更新HOUSE_STATE
            sql = "UPDATE " + HOUSE_INFO_TABLE_NAME + " SET " + HOUSE_STATE + "='" + house_state
                    + "' WHERE " + HOUSE_ID + "='" + house_id + "'";
            db.execSQL(sql);

        } catch (Exception e) {
            Log.i("LOG_TAG", "查询Patient信息出现问题：" + e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return item;
    }

    // ====== 获取所有需要UPdate的信息 ==== //
    public void getNeedUpdateInfoFromDb() {
        List<UpdateInfoItem> lists = new ArrayList<>();
        Set<String> needUpdateHouseIds = new TreeSet<>();

        Cursor cursor = null;
        try {
            String nowTime = simpleDateFormat.format(new Date(System.currentTimeMillis()));

            db = nurseDbHelper.getDbConnection();
            // 先查询有TAG_ID的patient信息
            String sql = "SELECT * FROM " + PATIENT_INFO_TABLE_NAME;
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext()) {
                if (!cursor.isNull(cursor.getColumnIndexOrThrow(TAG_ID))) {
                    UpdateInfoItem item = new UpdateInfoItem();

                    item.setTag_id(cursor.getString(cursor.getColumnIndexOrThrow(TAG_ID)));
                    item.setPatient_id(cursor.getString(cursor.getColumnIndexOrThrow(PATIENT_ID)));

                    lists.add(item);
                }
            }

            // 在根据TAG_ID查询需要更新的PATIENT信息
            for (int i = 0 ; i < lists.size(); i++) {
                UpdateInfoItem item = lists.get(i);

                String tag_id = item.getTag_id();
                sql = "SELECT * FROM " + TEMPER_INFO_TABLE_NAME + " WHERE " + TAG_ID + "='" + tag_id
                        + "' ORDER BY " + NEXT_TIME + " DESC LIMIT 1";
                cursor = db.rawQuery(sql, null);

                while (cursor.moveToNext()) {
                    // 判断是否需要更新
                    item.setIsNeedCheck(isNeedCheck(cursor.getString(cursor.getColumnIndexOrThrow(NEXT_TIME)), nowTime));
//                    Log.i("LOG_TAG", "TAG_ID :" + tag_id);
//                    Log.i("LOG_TAG", cursor.getString(cursor.getColumnIndexOrThrow(NEXT_TIME)));
//                    Log.i("LOG_TAG", nowTime);
//                    Log.i("LOG_TAG", String.valueOf(isNeedCheck(cursor.getString(cursor.getColumnIndexOrThrow(NEXT_TIME)), nowTime)));
                }
            }

            // 更新所有需要check的patient的bed信息
            for (UpdateInfoItem item : lists) {
                if (item.isNeedCheck()) {
                    String patient_id = item.getPatient_id();

                    // 更新BED_STATE
                    sql = "UPDATE " + BED_INFO_TABLE_NAME + " SET " + BED_STATE + "='uncheck' WHERE " + PATIENT_ID + "='" + patient_id +"'";
                    db.execSQL(sql);

                    // 查询出对应的HOUSE_ID
                    sql = "SELECT " + HOUSE_ID + " FROM " + BED_INFO_TABLE_NAME + " WHERE " + PATIENT_ID + "='" + patient_id +"'";
                    cursor = db.rawQuery(sql, null);
                    while (cursor.moveToNext()) {
                        needUpdateHouseIds.add(cursor.getString(cursor.getColumnIndexOrThrow(HOUSE_ID)));
                    }
                }
            }

            // 更新HOUSE_STATE
            for (String house_id : needUpdateHouseIds) {
                sql = "UPDATE " + HOUSE_INFO_TABLE_NAME + " SET " + HOUSE_STATE + "='uncheck' WHERE " + HOUSE_ID + "='" + house_id + "'";
                db.execSQL(sql);
            }
            Log.i("LOG_TAG", "getNeedUpdateInfoFromDb 更新完成");

        } catch (Exception e) {
            Log.i("LOG_TAG", "查询Patient信息出现问题：" + e.toString());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return;
    }

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 检查是否到达测量时间
    private static boolean isNeedCheck(String next_time, String nowTime) {
        return  next_time.compareTo(nowTime) <= 0;
    }

}



