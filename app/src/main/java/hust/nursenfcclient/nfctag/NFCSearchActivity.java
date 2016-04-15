package hust.nursenfcclient.nfctag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.database.DatabaseQueryHelper;
import hust.nursenfcclient.helps.NFC_Helper;
import hust.nursenfcclient.network.ServicesHelper;
import hust.nursenfcclient.patient.TemperInfoItem;

/**
 * Created by admin on 2015/12/3.
 */
public class NFCSearchActivity extends FragmentActivity {
    private FragmentManager fragmentManager;

    private TestSearchingFragment searchingFragment;
    private PairedFragment pairedFragment;
    private UnPairFragment unPairFragment;

    private TextView temperInfoHeadTxt;
    private ImageView returnBt;

    // NFC 相关参数
    private NFC_Helper nfc_helper;
    private NfcTagInfoItem nfcDataItem;

    private SharedPreferences sharedPreference;

    private MyHandler mHandler;

    private float mTouchSlop;

    // 设定一个LOCKED标志
    public static final int LOCKED = 1;
    public static final int UNLOCK = -1;
    private int LOCKED_STATUS = UNLOCK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_search_layout);

        sharedPreference = getSharedPreferences(MainActivity.SHAREDPR_NAME, MODE_PRIVATE);
        mHandler = new MyHandler(this);

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        EventBus.getDefault().register(this);
        fragmentManager = getSupportFragmentManager();
        initNFC();
        initView();

        onStartSearhing(getIntent());
    }

    // 初始化NFC相关参数
    private void initNFC() {
        nfc_helper = NFC_Helper.getInstance(getApplicationContext());
        nfc_helper.checkAndOpenNFC();
        nfc_helper.initnfc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfc_helper.enableForegroundDispatch(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfc_helper.disableForegroundDispatch(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 处理singleTop模式下重复扫描标签情况
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        onStartSearhing(intent);
    }

    private NfcTagInfoItem nfcTagInfoItem = null;

    private Intent curTagIntent = null;

    // 开始扫描标签，开启NFCSearingFramgnt
    private void onStartSearhing(Intent intent) {
        if (searchingFragment == null) {
            searchingFragment = new TestSearchingFragment();
            switchToView(searchingFragment);
            Log.i("LOG_TAG", "searchingFragment创建完成");


            mHandler.sendEmptyMessage(SHOW_CLICK_BUTTON); // 发送显示点击按钮
            curTagIntent = new Intent(intent);
        }

//        LOCKED_STATUS = LOCKED;
//        // 首先根据Intent读取TAG信息
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // 读取Intent消息
////                nfcTagInfoItem = nfc_helper.getTagIdFromIntentTest(intent);
//
//            }
//        }).start();

        // 开启读取TAG信息线程
//        new GetTagDataTask().execute(intent);
    }


    // 使用

    // 待写入数据
    public static final byte DATA_0 = 0x00;
    public static final byte DATA_1 = 0x01;
    public static final byte DATA_2 = 0x02;

    // 读取等待的时间
    private static final long WAITING_TIME = 5 * 1000;
    private static final long SLEEP_TIME = 10;

    private static final long DELAY_TIME = 1 * 1000; // 写入成功后等待1s读取温度值

    private int ClickBtTimes = 0;
    public static final int MAX_CLICK_TIME = 3;
    // 读取TAG信息线程
    private class GetTagDataTask extends AsyncTask<Intent, Void, NfcTagInfoItem> {

        @Override
        protected NfcTagInfoItem doInBackground(Intent... params) {
            NfcTagInfoItem dataItem = null;

            try {
                // ============= 具体的TAG读取流程 ==============//
                // Step1 先读取TAG UID
                dataItem = nfc_helper.getTagUIDFromIntentTest(params[0]);

                // 判断读取UID是否成功
                if ((dataItem != null) && (!dataItem.getTag_id().equals(""))) {
                    // Step2 尝试向0x7E写入0x01
                    long cur_time = System.currentTimeMillis();

                    mHandler.sendEmptyMessage(WRITING_DATA_INTO_TAG);
                    while (System.currentTimeMillis() - cur_time <= WAITING_TIME) {
                        boolean isSuccess = nfc_helper.wirteDataIntoTag(dataItem, DATA_1);

                        if (isSuccess) {
                            break;
                        } else {
                            // sleep 10ms后继续写
                            Thread.sleep(SLEEP_TIME);
                        }
                    }
                    mHandler.sendEmptyMessage(WRITIED_DATA_INTO_TAG);
                }

                // Step3 等1s后，开始读取温度值
                Thread.sleep(DELAY_TIME);

                // 开始读取温度值
                dataItem = nfc_helper.getTagInfoDelayed(dataItem);

                // 输出dateItem的相关参数
                Log.i(MainActivity.LOG_TAG, "dataItem.ReadTimes:" + dataItem.getReadTimes());
                Log.i(MainActivity.LOG_TAG, "dataItem.LastReadTimes:" + dataItem.getLastReadTimes());
                Log.i(MainActivity.LOG_TAG, "dataItem.isReadSuccess:" + dataItem.isReadSuccess());
                Log.i(MainActivity.LOG_TAG, "dataItem.Temper_num:" + dataItem.getTemper_num());

            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "GetTagDataTask Error:" + e.toString());
            }
            return dataItem;
        }

        @Override
        protected void onPostExecute(NfcTagInfoItem nfcTagInfoItem) {
            super.onPostExecute(nfcTagInfoItem);

            // 处理获得的结果
            onEndSearing(nfcTagInfoItem);
        }
    }

    // 处理从TAG信息获得结果
    private void onEndSearing(NfcTagInfoItem tagInfoItem) {
        boolean isSuccess = false;
        nfcDataItem = tagInfoItem;

        if (tagInfoItem == null || tagInfoItem.isTagLost()) {
            Toast.makeText(NFCSearchActivity.this, R.string.tag_lost_hint, Toast.LENGTH_SHORT).show();
        } else if (!tagInfoItem.isDataValid()) {
            Toast.makeText(NFCSearchActivity.this, R.string.data_not_valid, Toast.LENGTH_SHORT).show();
        } else {
            if (searchingFragment != null) {
                searchingFragment.showEndSearchingResult(true, tagInfoItem);
            }
        }

//        // 判断是否TAG信息是否获取成功
//        if (tagInfoItem != null && !tagInfoItem.isTagLost() && tagInfoItem.isReadSuccess()) {
//            isSuccess = true;
//        }


    }


    // 从标签中获取数据
    private NfcTagInfoItem getDataFromTag(Intent intent) {
        NfcTagInfoItem dataItem = null;
        if (intent != null) {
            try {
                if (nfc_helper != null) {
                    dataItem = nfc_helper.resolveMessageFromIntent(intent);
                }
//
                Log.i(MainActivity.LOG_TAG, "dataItem.ReadTimes:" + dataItem.getReadTimes());
                Log.i(MainActivity.LOG_TAG, "dataItem.LastReadTimes:" + dataItem.getLastReadTimes());
                Log.i(MainActivity.LOG_TAG, "dataItem.isReadSuccess:" + dataItem.isReadSuccess());

            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, e.toString());
            }
        }

        return dataItem;
    }

    public static final int TAG_PAIRED = 1;
    public static final int TAG_UNPAIR = -1;
    public static final int GET_DB_EXCEPTION = 0;

    // 处理读取TAG过程中的事件
    public static final int WRITING_DATA_INTO_TAG = 2;
    public static final int WRITIED_DATA_INTO_TAG = 3;

    // 显示点击Button
    public static final int SHOW_CLICK_BUTTON = 4;


    // 处理Handler事件
    private class MyHandler extends Handler {

        WeakReference<NFCSearchActivity> weakRef;

        public MyHandler(NFCSearchActivity activity) {
            weakRef = new WeakReference<NFCSearchActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            weakRef.get().handleMessage(msg);
        }
    }

    // 处理Handler事件
    private void handleMessage(Message msg) {
        switch (msg.what) {
            case TAG_PAIRED:
                EventBus.getDefault().post(new NFCSearchEvent(NFCSearchEvent.POPUP_PAIRED_WINDOW_EVENT));
                break;
            case TAG_UNPAIR:
                EventBus.getDefault().post(new NFCSearchEvent(NFCSearchEvent.POPUP_UNPAIR_WINDOW_EVENT));
                break;
            case GET_DB_EXCEPTION:
                Toast.makeText(NFCSearchActivity.this, R.string.query_tag_failed, Toast.LENGTH_SHORT).show();
                closeView();
                break;
            case WRITING_DATA_INTO_TAG: // 正在尝试向TAG中写入数据
                if (searchingFragment != null)
                    searchingFragment.onWritingData();
                break;
            case WRITIED_DATA_INTO_TAG: // 数据写入之后或者过了5s之后，popWindow消失
                if (searchingFragment != null)
                    searchingFragment.onDataWrited();
                break;

            case SHOW_CLICK_BUTTON:
                if (searchingFragment != null)
                    searchingFragment.onShowOnClickButton();
                break;
        }
    }

    // 处理EventBus事件
    public void onEventMainThread(NFCSearchEvent event) {

        switch (event.getAction()) {
            case NFCSearchEvent.PAIR_FAILED_EVENT:
                break;
            case NFCSearchEvent.PAIR_SUCCESS_EVENT:
                onPairSuccess(phraseNFCData(nfcDataItem));
                break;
            case NFCSearchEvent.POPUP_UNPAIR_WINDOW_EVENT:
                showUnPiarWindow(phraseNFCData(nfcDataItem));
                break;
            case NFCSearchEvent.POPUP_PAIRED_WINDOW_EVENT:
                showPairedWindow(phraseNFCData(nfcDataItem));
                break;
            case NFCSearchEvent.CLOSE_EVENT:
                break;
            case NFCSearchEvent.SHOW_PIAR_OR_UNPAIR_WINDOW:
                onShowPairOrUnPairWindow(nfcDataItem);
                break;
            case NFCSearchEvent.READ_TEMPER: // 点击读取温度按钮，开始读取温度
                // 读取失败，关闭界面
                if (ClickBtTimes > MAX_CLICK_TIME) {
                    ClickBtTimes = 0;
                    if (searchingFragment != null) {
                        searchingFragment.showEndSearchingResult(false, null);
                    }
                } else {
                    // 执行获取温度线程
                    new DelayedGetTagDataTask().execute(curTagIntent);
                    ClickBtTimes++;
                }
                break;
        }
    }

    // 读取TAG信息线程
    private class DelayedGetTagDataTask extends AsyncTask<Intent, Void, NfcTagInfoItem> {

        @Override
        protected NfcTagInfoItem doInBackground(Intent... params) {
            NfcTagInfoItem dataItem = null;
            try {
                // 开始读取温度值
                dataItem = nfc_helper.resolveMessageFromIntent(params[0]);

                // 输出dateItem的相关参数
                Log.i(MainActivity.LOG_TAG, "dataItem.ReadTimes:" + dataItem.getReadTimes());
                Log.i(MainActivity.LOG_TAG, "dataItem.LastReadTimes:" + dataItem.getLastReadTimes());
                Log.i(MainActivity.LOG_TAG, "dataItem.isReadSuccess:" + dataItem.isReadSuccess());
                Log.i(MainActivity.LOG_TAG, "dataItem.Temper_num:" + dataItem.getTemper_num());

            } catch (Exception e) {
                Log.e(MainActivity.LOG_TAG, "DelayedGetTagDataTask Error:" + e.toString());
            }
            return dataItem;
        }

        @Override
        protected void onPostExecute(NfcTagInfoItem nfcTagInfoItem) {
            super.onPostExecute(nfcTagInfoItem);

            // 解锁
//            LOCKED_STATUS = UNLOCK;
            // 处理获得的结果
            onEndSearing(nfcTagInfoItem);
        }
    }

    // 处理NFC扫描结束后事件
    private void onShowPairOrUnPairWindow(NfcTagInfoItem dataItem) {
        // 读取信息失败
        if (dataItem == null || !dataItem.isReadSuccess()) {
            closeView();
        } else {
            final String tag_id = dataItem.getTag_id();

            // 开启查询数据库线程
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(isTagPaired(tag_id));
                }
            }).start();
            searchingFragment = null;
        }
    }

    // 判断TAG_ID是否匹配成功
    private int isTagPaired(String tag_id) {
        int result = DatabaseQueryHelper.GET_DB_EXCEPTION;
        try {
            result = DatabaseQueryHelper.getInstance(getApplicationContext()).getIsTagPairedFromDb(tag_id);
        } catch (Exception e) {
        }
        Log.w(MainActivity.LOG_TAG, "isTagPaired==" + result);
        return result;
    }


    // 将NfcTagInfoItem解析为TemperInfoItem
    private TemperInfoItem phraseNFCData (NfcTagInfoItem tagInfoItem) {
        TemperInfoItem temper_item = null;
        if (tagInfoItem != null) {
            temper_item = new TemperInfoItem();
            temper_item.setTag_id(tagInfoItem.getTag_id());
            temper_item.setTemper_num(tagInfoItem.getTemper_num());
            temper_item.setNurse_id(sharedPreference.getString(MainActivity.SHARED_NURSE_ID, MainActivity.DEFAULT_NURSE_ID));

            Log.w(MainActivity.LOG_TAG, "temper_item.getNurse_id == " + temper_item.getNurse_id());
            temper_item.setLast_time(tagInfoItem.getLast_time());
            temper_item.setNext_time(tagInfoItem.getNext_time());
        }
        return temper_item;
    }

    // ==== 匹配成功情况 ==== //
    private void onPairSuccess(final TemperInfoItem dataItem) {
        if (unPairFragment != null) {
            View view = unPairFragment.getView();

            ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0f);
            animator.setDuration(1000).start();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    showPairedWindow(dataItem);
                }
            });
        }
    }

    // === 显示Unpair情况 ==== //
    private void showUnPiarWindow(TemperInfoItem dataItem) {
        if (dataItem != null) {
            Toast.makeText(this, R.string.to_pair_hint, Toast.LENGTH_SHORT).show();

            unPairFragment = new UnPairFragment();
            // 传递Tag信息
            Bundle datas = new Bundle();
            datas.putString(ServicesHelper.TAG_ID, dataItem.getTag_id());
            datas.putFloat(ServicesHelper.TEMPER_NUM, dataItem.getTemper_num());
            datas.putString(ServicesHelper.NURSE_ID, dataItem.getNurse_id());
            unPairFragment.setArguments(datas);

            switchToView(unPairFragment);
        }
    }

    // ====== 显示Pair情况 ==== //
    private void showPairedWindow(TemperInfoItem dataItem) {
        if (dataItem != null) {
            pairedFragment = new PairedFragment();
            Bundle datas = new Bundle();
            datas.putString(ServicesHelper.TAG_ID, dataItem.getTag_id());
            datas.putFloat(ServicesHelper.TEMPER_NUM, dataItem.getTemper_num());
            datas.putString(ServicesHelper.NURSE_ID, dataItem.getNurse_id());
            datas.putString(ServicesHelper.LAST_TIME, dataItem.getLast_time());
            datas.putString(ServicesHelper.NEXT_TIME, dataItem.getNext_time());
            pairedFragment.setArguments(datas);

            switchToView(pairedFragment);
        }
    }

    // 转换界面
    private void switchToView(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_container, fragment).commit();
        }
    }

    // 初始化界面
    private void initView() {
        temperInfoHeadTxt = $(R.id.temperInfoHeadTxt);
        returnBt = $(R.id.returnBt);

        returnBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeView();
            }
        });
    }

    // 关闭本界面
    private void closeView() {
//        startActivity(new Intent(NFCSearchActivity.this, MainActivity.class));
        NFCSearchActivity.this.finish();
    }

    private <T> T $(int resId) {
        return (T) findViewById(resId);
    }
}

