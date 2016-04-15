package hust.nursenfcclient.helps;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcV;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.init.LogInActivity;
import hust.nursenfcclient.network.ServicesHelper;
import hust.nursenfcclient.nfctag.NFCSearchActivity;
import hust.nursenfcclient.nfctag.NfcTagInfoItem;

/**
 * Created by admin on 2015/12/5.
 */
public class NFC_Helper {
    private static NFC_Helper instance = null;
    private Context mContext;

    private NfcAdapter nfcAdapter;
    private IntentFilter[] intentFiltersArray; //用于声明想要拦截的Intent的Intent Filter数组
    private PendingIntent pendingIntent;     //用于打包Tag Intent的Intent
    private String[][] techListArray;     //想要处理的标签技术的数组

    // NFC的相关参数信息
    private int blockNumber; // 数据块个数
    private int oneBlockSize; // 一个数据块的大小

    // 时间格式
    private SimpleDateFormat dateFormat = new SimpleDateFormat(ServicesHelper.DATE_FORMAT);
    private SharedPreferences sharedPreferences;

    private NFC_Helper(Context context) {
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(MainActivity.SHAREDPR_NAME, Context.MODE_PRIVATE);
    }

    public static NFC_Helper getInstance(Context context) {
        if (instance == null)
            instance = new NFC_Helper(context);
        return instance;
    }

    // 获取NFCAdapter
    public NfcAdapter getNfcAdapter() {
        if (nfcAdapter == null)
            nfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
        return nfcAdapter;
    }

    // 检查NFC是否开启
    public boolean isNfcOpened() {
        nfcAdapter = getNfcAdapter();
        boolean isOpened = true;

        if (nfcAdapter == null) {
            Toast.makeText(mContext, R.string.has_no_nfc, Toast.LENGTH_SHORT).show();
            isOpened= false;
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(mContext, R.string.nfc_not_open, Toast.LENGTH_SHORT).show();
            isOpened = false;
        }
        return isOpened;
    }

    // 检查NFC是否开启，如果未开启，直接开启NFC
    public void checkAndOpenNFC() {
        nfcAdapter = getNfcAdapter();
        if (nfcAdapter == null) {
            Toast.makeText(mContext, R.string.has_no_nfc, Toast.LENGTH_SHORT).show();
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(mContext, R.string.nfc_not_open_hint, Toast.LENGTH_SHORT).show();
            // 跳转到NFC设置界面
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }


    // 对NFC一些协议的初始化
    public void initnfc() {
        // 用来封装NFC标签的详细信息
        pendingIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, NFCSearchActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        if (techListArray == null)
            // 表示支持的相关NFC技术的类
            techListArray = new String[][]{new String[]{NfcV.class.getName()},
                    new String[]{MifareClassic.class.getName()},
                    new String[]{NdefFormatable.class.getName()}};

        if (intentFiltersArray == null) {
            // 生命Intent Filter
            IntentFilter ndefFilter = new IntentFilter("android.nfc.action.NDEF_DISCOVERED");
            IntentFilter tagFilter = new IntentFilter("android.nfc.action.TAG_DISCOVERED");
            IntentFilter techFilter = new IntentFilter("android.nfc.action.Tech_DISCOVERED");
            try {
                ndefFilter.addDataType("*/*");
            } catch (IntentFilter.MalformedMimeTypeException e) {
                throw new RuntimeException("fail", e);
            }
            intentFiltersArray = new IntentFilter[]{ndefFilter, techFilter, tagFilter};
        }
    }

    // 开启前台分派机制
    public void enableForegroundDispatch(Activity activity) {
        nfcAdapter = getNfcAdapter();
        if (nfcAdapter == null) {
            Toast.makeText(mContext, R.string.has_no_nfc, Toast.LENGTH_SHORT).show();
        } else {
//            initnfc();
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techListArray);
            Log.i("LOG_TAG", "enableForegroundDispatch");
        }
    }

    // 关闭前台分派机制
    public void disableForegroundDispatch(Activity activity) {
        nfcAdapter = getNfcAdapter();

        if (nfcAdapter == null) {
            Toast.makeText(mContext, R.string.has_no_nfc, Toast.LENGTH_SHORT).show();
        } else {
            nfcAdapter.disableForegroundDispatch(activity);
        }
        Log.i("LOG_TAG", "disableForegroundDispatch");
    }

    // ====== 避免多次读取失败的测试方法 ======//
    public NfcTagInfoItem getTagIdFromIntentTest(Intent intent) {
        NfcTagInfoItem infoItem = null;
        nfcAdapter = getNfcAdapter();
        Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);
        String tag_id = "";
        switch (intent.getAction()) {
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                try {
                    infoItem = new NfcTagInfoItem();
                    // 读取UID
                    NfcV mNfcV = NfcV.get(tag);
                    mNfcV.connect();

                    Log.d(MainActivity.LOG_TAG, "getTagIdFromIntentTest Connected");
                    infoItem.setmNfcV(mNfcV);

                    tag_id = getUid(mNfcV);
                    infoItem.setTag_id(tag_id);

                    // 获取Block相关Information
                    int[] blockInfo = getBlockInfo(mNfcV);
                    if ((blockInfo != null) && (blockInfo.length == 2)) {
                        infoItem.setBlockNumber(blockInfo[0]);  // 设置BlockNumber
                        infoItem.setOneBlockSize(blockInfo[1]); // 设置BlockSize
                    }

                    // 获取读取次数
                    byte readTimes = getReadTimes(infoItem.getBlockNumber(), mNfcV);
                    infoItem.setLastReadTimes(readTimes);

                } catch (Exception e) {
                    Log.i(MainActivity.LOG_TAG, "getTagIdFromIntent" + e.toString());
                }
                break;
        }
        return infoItem;
    }

    // ======== 仅读取TAG的 UID，保存到NfcTagInfoItem中 ======== //
    public NfcTagInfoItem getTagUIDFromIntentTest(Intent intent) {
        NfcTagInfoItem infoItem = null;
        nfcAdapter = getNfcAdapter();
        Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);
        String tag_id = "";
        switch (intent.getAction()) {
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                try {
                    infoItem = new NfcTagInfoItem();
                    // 读取UID
                    NfcV mNfcV = NfcV.get(tag);
                    mNfcV.connect();

                    Log.d(MainActivity.LOG_TAG, "getTagIdFromIntentTest Connected");
                    infoItem.setmNfcV(mNfcV);

                    tag_id = getUid(mNfcV);
                    infoItem.setTag_id(tag_id);

                    // 获取Block相关Information
                    int[] blockInfo = getBlockInfo(mNfcV);
                    if ((blockInfo != null) && (blockInfo.length == 2)) {
                        infoItem.setBlockNumber(blockInfo[0]);  // 设置BlockNumber
                        infoItem.setOneBlockSize(blockInfo[1]); // 设置BlockSize
                    }

                } catch (Exception e) {
                    Log.i(MainActivity.LOG_TAG, "getTagIdFromIntent" + e.toString());
                }
                break;
        }
        return infoItem;
    }


    // 获取温度数据(延时一段时间后获取)
    public NfcTagInfoItem getTagInfoOnClickDelayed(NfcTagInfoItem infoItem) throws IOException {
        if (infoItem == null)
            return null;

        NfcV mNfcV = infoItem.getmNfcV();
//        mNfcV.connect();
        // 获取读取次数
        byte readTimes = getReadTimes(infoItem.getBlockNumber(), mNfcV);
        infoItem.setReadTimes(readTimes);

        // 获取测量温度
        float temper_num = getTemperNum(infoItem.getBlockNumber(), mNfcV);

        infoItem.setTemper_num(temper_num);

        // 设置当前测量时间
        infoItem.setLast_time(dateFormat.format(new Date(System.currentTimeMillis())));
        long delay_time = sharedPreferences.getLong(MainActivity.SHARED_DELAY_TIME, MainActivity.DEFAULT_DELAY_TIME);
        infoItem.setNext_time(dateFormat.format(new Date(System.currentTimeMillis() + delay_time)));

        Log.i(MainActivity.LOG_TAG, "readTimes: ===>" + readTimes);
        Log.i(MainActivity.LOG_TAG, "temper_num ==" + temper_num);
        return infoItem;
    }

    // 读取数据
    public NfcTagInfoItem getTagInfoDelayed(NfcTagInfoItem infoItem) throws IOException {
        if (infoItem == null)
            return null;

        NfcV mNfcV = infoItem.getmNfcV();
//        mNfcV.connect();
        // 判断是否读取成功
        byte readData = getReadTimes(infoItem.getBlockNumber(), mNfcV);
        infoItem.setIsReadSuccess(readData == NFCSearchActivity.DATA_2);

        // 获取测量温度
        float temper_num = getTemperNum(infoItem.getBlockNumber(), mNfcV);
        infoItem.setTemper_num(temper_num);

        // 设置当前测量时间
        infoItem.setLast_time(dateFormat.format(new Date(System.currentTimeMillis())));
        long delay_time = sharedPreferences.getLong(MainActivity.SHARED_DELAY_TIME, MainActivity.DEFAULT_DELAY_TIME);
        infoItem.setNext_time(dateFormat.format(new Date(System.currentTimeMillis() + delay_time)));

        Log.i(MainActivity.LOG_TAG, "isSuccess: ===>" + infoItem.isReadSuccess());
        Log.i(MainActivity.LOG_TAG, "temper_num ==" + temper_num);
        return infoItem;
    }

    // 向TAG中写入数据
    public boolean wirteDataIntoTag(NfcTagInfoItem infoItem, byte data) {
        if (infoItem == null)
            return false;

        NfcV mNfcV = infoItem.getmNfcV();

        return writeOneBlock(mNfcV, 0x7E, data);
    }


    // 读取数据
    public NfcTagInfoItem resolveMessageFromIntent (Intent intent) {
        nfcAdapter = getNfcAdapter();
        Tag tag = intent.getParcelableExtra(nfcAdapter.EXTRA_TAG);

        NfcTagInfoItem infoItem = null;

        switch (intent.getAction()) {
            case NfcAdapter.ACTION_NDEF_DISCOVERED:
                Toast.makeText(mContext, "ACTION_NDEF_DISCOVERED", Toast.LENGTH_SHORT).show();
                break;
            case NfcAdapter.ACTION_TECH_DISCOVERED:
                try {
                    infoItem = getInfoFromTag(tag);
                } catch (Exception e) {
                    Log.i(MainActivity.LOG_TAG, "resolveMessageFromIntent" + e.toString());
                    infoItem = new NfcTagInfoItem();
                    infoItem.setIsTagLost(true); // 设置标签Lost错误
                }
                break;
            case NfcAdapter.ACTION_TAG_DISCOVERED:
                Toast.makeText(mContext, "ACTION_TAG_DISCOVERED", Toast.LENGTH_SHORT).show();
                break;
        }
        return infoItem;
    }

    // 从TAG标签中读取所有相关标签信息
    public NfcTagInfoItem getInfoFromTag(Tag tag) throws IOException{
        NfcTagInfoItem item = new NfcTagInfoItem();

        NfcV mNfcV = NfcV.get(tag);
        mNfcV.connect();

        String tag_id = getUid(mNfcV);
        // 获取UID
        item.setTag_id(tag_id);

        // 获取Block相关Information
        int[] blockInfo = getBlockInfo(mNfcV);
        if ((blockInfo != null) && (blockInfo.length == 2)) {
            item.setBlockNumber(blockInfo[0]);  // 设置BlockNumber
            item.setOneBlockSize(blockInfo[1]); // 设置BlockSize
        }

        // 获取读取次数
        byte readTimes = getReadTimes(item.getBlockNumber(), mNfcV);
        item.setReadTimes(readTimes);

        // 设置上次读取次数
        item.setLastReadTimes(sharedPreferences.getInt(tag_id, MainActivity.DEFAULT_READ_TIMES));

//        // 判断是否读取成功，如果读取成功，则修改缓存中的该TAG的读取次数
//        if (item.isReadSuccess()) {
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putInt(tag_id, readTimes);
//            editor.commit();
//        }
        // 无论是否读取成功都应该将该次TAG中读取的次数缓存起来
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(tag_id, readTimes);
        editor.commit();

        // ************************************ //
        // 设置读取TAG成功
        item.setIsReadSuccess(true);

        // 获取测量温度
        float temper_num = getTemperNum(item.getBlockNumber(), mNfcV);

        item.setTemper_num(temper_num);

        // 设置当前测量时间
        item.setLast_time(dateFormat.format(new Date(System.currentTimeMillis())));
        long delay_time = sharedPreferences.getLong(MainActivity.SHARED_DELAY_TIME, MainActivity.DEFAULT_DELAY_TIME);
        item.setNext_time(dateFormat.format(new Date(System.currentTimeMillis() + delay_time)));

        Log.i(MainActivity.LOG_TAG, "readTimes: ===>" + readTimes);
        Log.i(MainActivity.LOG_TAG, "temper_num ==" + temper_num);
        return item;
    }


    // 获得BlockSize及BlockNum
    private int[] getBlockInfo(NfcV mNfcV) throws IOException{
        if (mNfcV != null) {
            byte[] IDs = mNfcV.getTag().getId();
            // 获取Block Size
            byte[] cmd = new byte[10];
            cmd[0] = (byte) 0x22; //flag
            cmd[1] = (byte) 0x2B; //command
            System.arraycopy(IDs, 0, cmd, 2, IDs.length); // UID
            byte[] infoRmation = mNfcV.transceive(cmd);
            blockNumber = infoRmation[12];
            oneBlockSize = infoRmation[13];
            ++blockNumber;
            ++oneBlockSize;
//            Log.i(MainActivity.LOG_TAG, "blockNumber:==" + (blockNumber));
//            Log.i(MainActivity.LOG_TAG, "oneBlockSize:==" + (oneBlockSize));

            int[] result = new int[2];
            result[0] = blockNumber;
            result[1] = oneBlockSize;
            return result;
        }
        return null;
    }

    /* 读取一个Block的数据 */
    public byte[] readOneBlock(int position, NfcV mNfcV) throws IOException{
        try {
            byte[] cmds = new byte[]{(byte) 0x02, (byte) 0x20, (byte) position};
//        Log.i(MainActivity.LOG_TAG, "position==0x" + Integer.toHexString(position));

//        mNfcV.close();
//        mNfcV.connect();
            byte res[] = mNfcV.transceive(cmds);

            if(res[0] == 0x00){
                byte block[] = new byte[res.length - 1];
                System.arraycopy(res, 1, block, 0, res.length - 1);

                return block;
            }
        } catch (IOException e) {
            Log.e(MainActivity.LOG_TAG, "readOneBlock Failed, Error: " + e.toString());
        }
        return null;
    }

    /* 写一个block的数据 */
    public boolean writeOneBlock(NfcV mNfcV, int position, byte data){
        boolean isSuccess = false;
        try {
            byte[] cmds = new byte[]{(byte) 0x02, (byte) 0x21, (byte)position, 0, 0, 0, data};

            // 执行写操作
//            mNfcV.close();
//            mNfcV.connect();
            byte res[] = mNfcV.transceive(cmds);
            if (res[0] == 0x00) {
                // 写入成功
                isSuccess = true;
            }
        } catch (IOException e) {
            isSuccess = false;
            Log.e(MainActivity.LOG_TAG, "writeOneBlock Failed, Error: " + e.toString());
        }
        return isSuccess;
    }

    // 获得读取次数
    private byte getReadTimes(int blockNumber, NfcV mNfcV) throws IOException {
        byte times = -1;
        int readPos = blockNumber - 2;
        byte[] datas = readOneBlock(readPos, mNfcV);
        if ((datas != null) && (datas.length == 4))
            times = (byte)(datas[datas.length - 1] & 0x7F);
        return times;
    }

    // 获取温度值
    private byte[] getTemperNumBytes(int blockNumber, NfcV mNfcV) throws IOException {
        int readPos = blockNumber - 1;
        byte[] datas = readOneBlock(readPos, mNfcV);
        return datas;
    }

    private float getTemperNum(int blockNumber, NfcV mNfcV) throws IOException {
        float temper_num = 0;

        byte[] temper_bytes = getTemperNumBytes(blockNumber, mNfcV);

        if (temper_bytes != null) {
            temper_num = Float.valueOf(new String(temper_bytes));
        }
        return temper_num / 100;
    }


    // 获得UID
    private String getUid(NfcV mNfcV) throws IOException{
        if (mNfcV != null) {
            byte[] ID = mNfcV.getTag().getId();
            byte[] uid = new byte[ID.length];
            int j = 0;
            for (int i = ID.length - 1; i >= 0; i--) {
                uid[j] = ID[i];
                j++;
            }
            return bytesToHexString(ID);
        }
        return null;
    }

//    /*@function	:读取从begin开始end个block
//    @instructions	:begin + count 不能超过blockNumber
//    @param :begin block开始位置
//    @param :count 读取block数量
//    @return :返回内容字符串
//    */
//    public String readBlocks(int begin, int count) throws IOException{
//        if((begin + count)>blockNumber){
//            count = blockNumber - begin;
//        }
//        StringBuffer data = new StringBuffer();
//
//        for(int i = begin; i<=count + begin; i++){
//            data.append(readOneBlock(i));
//        }
//        return data.toString();
//
//    }

    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];

        for (int i = src.length - 1; i >= 0 ; i--) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }

        Log.w(MainActivity.LOG_TAG, "UID == " + stringBuilder.toString());
        return stringBuilder.toString();
    }

}
