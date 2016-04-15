package hust.nursenfcclient.nfctag;

import android.nfc.tech.NfcV;

/**
 * Created by admin on 2015/12/8.
 */
public class NfcTagInfoItem {
    private String tag_id;
    private float temper_num;
    private int readTimes;
    private int lastReadTimes; // 上一次该TAG的测量次数记录
    private String last_time;
    private String next_time;
    private int blockNumber; // 数据块个数
    private int oneBlockSize; // 一个数据块的大小
    private NfcV mNfcV;
    private boolean isReadSuccess;
    private boolean isTagLost;

    public static final float LOWEST_TEMPER_NUM = 11.00f;
    public static final float HIGHEST_TEMPER_NUM = 99.00f;

    public static final float MIN_TEMPER_NUM = 35.00f;
    public static final float MAX_TEMPER_NUM = 42.00f;
    public static final float LOWER_ILL_TEMPER_NUM = 36.00f;
    public static final float HIGHER_ILL_TEMPER_NUM = 38.50f;


    public NfcTagInfoItem() {

    }

    public NfcTagInfoItem(String tag_id, float temper_num, int readTimes, int lastReadTimes, String last_time, String next_time) {
        this.tag_id = tag_id;
        this.temper_num = temper_num;
        this.readTimes = readTimes;
        this.last_time = last_time;
        this.next_time = next_time;
        this.lastReadTimes = lastReadTimes;
    }

    public NfcV getmNfcV() {
        return mNfcV;
    }

    public void setmNfcV(NfcV mNfcV) {
        this.mNfcV = mNfcV;
    }

    public int getLastReadTimes() {
        return lastReadTimes;
    }

    public void setLastReadTimes(int lastReadTimes) {
        this.lastReadTimes = lastReadTimes;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public int getOneBlockSize() {
        return oneBlockSize;
    }

    public void setOneBlockSize(int oneBlockSize) {
        this.oneBlockSize = oneBlockSize;
    }

    public String getLast_time() {
        return last_time;
    }

    public void setLast_time(String last_time) {
        this.last_time = last_time;
    }

    public String getNext_time() {
        return next_time;
    }

    public void setNext_time(String next_time) {
        this.next_time = next_time;
    }

    public int getReadTimes() {
        return readTimes;
    }

    public void setReadTimes(int readTimes) {
        this.readTimes = readTimes;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public float getTemper_num() {
        if (temper_num <= MIN_TEMPER_NUM)
            return MIN_TEMPER_NUM;
        if (temper_num >= MAX_TEMPER_NUM)
            return MAX_TEMPER_NUM;
        return temper_num;
    }

    public void setTemper_num(float temper_num) {
        this.temper_num = temper_num;
    }

    public void setIsReadSuccess(boolean isSuccess) {
        isReadSuccess = isSuccess;
    }

    // ==== 判断是否读取成功 ===== //
    public boolean isReadSuccess() {
        return isReadSuccess;
    }

    public boolean isTagLost() {
        return isTagLost;
    }

    public void setIsTagLost(boolean isTagLost) {
        this.isTagLost = isTagLost;
    }

    public boolean isDataValid() {
        return readTimes != lastReadTimes;
    }
}
