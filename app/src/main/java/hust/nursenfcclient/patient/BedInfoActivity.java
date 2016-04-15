package hust.nursenfcclient.patient;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.handmark.pulltorefresh.library.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.helps.NFC_Helper;
import hust.nursenfcclient.house.AnimationListView;
import hust.nursenfcclient.house.HouseInfoItem;
import hust.nursenfcclient.house.PullToRefreshAnimationListView;
import hust.nursenfcclient.database.DatabaseQueryHelper;
import hust.nursenfcclient.helps.ChartHelper;
import hust.nursenfcclient.imageloader.ImageHelper;

/**
 * Created by admin on 2015/11/23.
 */
public class BedInfoActivity extends Activity implements View.OnClickListener {

    private TextView bedInfoHeadTxt;
    private ImageView returnBt;
    private PullToRefreshAnimationListView bedInfoList;
    private List<PatientInfoItem> dataLists = new ArrayList<>();
    private PatientInfoAdapter mAdapter;

    // 弹出详细信息界面
    private PopupWindow popupWindow;
    private View popView;
    private LayoutInflater inflater;
    private LineChart mTemperChart;

    public static final float MIN_TEMPER = 35.0f;
    public static final float MAX_TEMPER = 43.0f;
    public static final float UPPER_TEMPER = 38.5f;
    public static final float LOWER_TEMPER = 36.0f;

    public static final String BEDID_INFOS = "bed_ids";
    private String bedIDsStr;
    private String[] bedIds;
    private String houseId;

    // ==== 温度测量的相关信息 ===== //
    private List<TemperInfoItem> temperDatalist;

    // 滑动距离
    private float mTouchSlop;

    // ===== NFC相关 ==== //
    private NFC_Helper nfc_helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bed_info_layout);

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        // 注册EventBus
        EventBus.getDefault().register(this);
        bedIDsStr = getIntent().getExtras().getString(BEDID_INFOS);
        Log.i("LOG_TAG", bedIDsStr);

        inflater = LayoutInflater.from(this);

        initView();
        initData();
        initNFC();
    }

    // 初始化NFC相关参数
    private void initNFC() {
        nfc_helper = NFC_Helper.getInstance(getApplicationContext());
        nfc_helper.checkAndOpenNFC();
        nfc_helper.initnfc();
    }

    // 刷新界面
    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
        nfc_helper.enableForegroundDispatch(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfc_helper.disableForegroundDispatch(this);
    }

    // 刷新加载数据
    private void refreshData() {
        try {
            dataLists = DatabaseQueryHelper.getInstance(getApplicationContext()).getPatientInfoFromDb(bedIds);
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {}
    }

    // 加载数据
    private void initData() {
        try {
            String[] datas = bedIDsStr.split(" ");
            houseId = datas[0];
            bedIds = new String[datas.length - 1];

            for (int i = 0; i < bedIds.length; i++)
                bedIds[i] = datas[i + 1];

            bedInfoHeadTxt.setText(houseId + "房");

            dataLists = DatabaseQueryHelper.getInstance(getApplicationContext()).getPatientInfoFromDb(bedIds);
            mAdapter = new PatientInfoAdapter(dataLists, this);

            // 添加OnRefreshListener
            bedInfoList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<AnimationListView>() {
                @Override
                public void onRefresh(PullToRefreshBase<AnimationListView> refreshView) {
                    String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
                            DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

                    // Update the LastUpdatedLabel
                    refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

                    // 执行下拉刷新逻辑任务
                    new GetDataTask().execute();
                }
            });

            ListView actualListView = bedInfoList.getRefreshableView();
            actualListView.setAdapter(mAdapter);

            actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if ((id >= 0) && (id < dataLists.size()))
                        popUpPatientWindow((int)id);
                }
            });

        } catch (Exception e) {
            Log.e("LOG_TAG", e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        returnBt = $(R.id.returnBt);
        returnBt.setOnClickListener(this);
        bedInfoList = $(R.id.bedInfoList);
        bedInfoHeadTxt = $(R.id.bedInfoHeadTxt);
    }

    private <T> T $(int resId) {
        return (T) findViewById(resId);
    }

    private <T> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.returnBt:
                BedInfoActivity.this.finish();
                break;
        }
    }

    // 处理EventBus事件
    // 响应Service发送来的Event
    public void onEventMainThread(BedInfoEvent event) {
        HouseInfoItem item = event.getmItem();
        Log.i("LOG_TAG", item.getHouseId());
        Toast.makeText(BedInfoActivity.this, item.getHouseId(), Toast.LENGTH_SHORT).show();
    }


    // 弹出详细信息界面
    private void popUpPatientWindow(int id) {
        PatientInfoItem item = dataLists.get(id);

        popView = inflater.inflate(R.layout.patient_info_layout, null);
        // 初始化基本信息
        initPopupPatientInfo(popView, item);

        // 表格控件使用
        mTemperChart = $(popView, R.id.mTemperChart);

        // === 从数据库中查询温度测量信息 ===== //
        temperDatalist = DatabaseQueryHelper.getInstance(getApplicationContext()).getTemperInfoFromDb(item.getTag_id());

        // === 测试温度表 ====
        LineData data = ChartHelper.getData(temperDatalist, MIN_TEMPER, MAX_TEMPER, this);
        ChartHelper.setupChart(mTemperChart, data);

        popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);

        popupWindow.showAtLocation(popView, Gravity.CENTER, 0, 0);

    }

    // === 初始化基本信息 ==== //
    private void initPopupPatientInfo(View popView, PatientInfoItem item) {
        TextView bedIdText         = $ (popView, R.id.bedIdText);
        TextView bedStateText      = $ (popView, R.id.bedStateText);
        TextView patientIdText     = $ (popView, R.id.patientIdText);
        TextView patientNameText   = $ (popView, R.id.patientNameText);
        TextView patientAgeText    = $ (popView, R.id.patientAgeText);
        ImageView patientGenderImg = $ (popView, R.id.patientGenderImg);
        TextView patientRecordText = $ (popView, R.id.patientRecordText);
        TextView lastTemperText    = $ (popView, R.id.lastTemperText);
        ImageView patientPhotoImg  = $ (popView, R.id.patientPhotoImg);

        // 设置界面
        try {
            bedIdText.setText(item.getBedId());
            bedStateText.setText(item.getBedState());
            patientIdText.setText(item.getPatientId());
            patientNameText.setText(item.getPatientName());
            patientAgeText.setText(String.valueOf(item.getPatientAge()));
            patientRecordText.setText(item.getPatientRecord());
            lastTemperText.setText(String.valueOf(item.getLastTemper()) + "度");


            if (item.isChecked())
                bedStateText.setTextColor(getResources().getColor(R.color.gray));
            else
                bedStateText.setTextColor(getResources().getColor(R.color.red));

            if (item.getPatientGender())
                patientGenderImg.setImageResource(R.mipmap.male_icon);
            else
                patientGenderImg.setImageResource(R.mipmap.femal_icon);

            ImageHelper.setImageRaw(patientPhotoImg, item.getPatientPhoto());

        } catch (Exception e) {
            Log.i("LOG_TAG", "PatientInfoAdapter 加载数据出现错误：" + e.toString());
        }
    }


    // 向左滑动关闭Activity
    private int lastX;
    private final static int MUTI = 30;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                break;
            case MotionEvent.ACTION_UP:
                int curX = (int) event.getX();
                Log.i(MainActivity.LOG_TAG, "curX - lastX == " + (curX - lastX));

                // 如果左滑超过一定距离，则关闭Activity
                if ((curX - lastX) >= ((int) mTouchSlop * MUTI)) {
                    BedInfoActivity.this.finish();
                }
                break;
        }
        return true;
    }

    // 异步获取数据模块
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            // Simulates a background job.
            try {
                Thread.sleep(1000);
                dataLists = DatabaseQueryHelper.getInstance(getApplicationContext()).getPatientInfoFromDb(bedIds);
            } catch (InterruptedException e) {
            }
            return null;
        }

        // 下拉刷新之后的响应
        @Override
        protected void onPostExecute(String[] result) {
            mAdapter.notifyDataSetChanged();
            bedInfoList.onRefreshComplete();
            super.onPostExecute(result);
        }
    }
}
