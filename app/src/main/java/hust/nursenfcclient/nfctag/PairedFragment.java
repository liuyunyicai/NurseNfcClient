package hust.nursenfcclient.nfctag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import java.text.DecimalFormat;
import java.util.List;

import hust.nursenfcclient.R;
import hust.nursenfcclient.database.DatabaseQueryHelper;
import hust.nursenfcclient.helps.ChartHelper;
import hust.nursenfcclient.imageloader.ImageHelper;
import hust.nursenfcclient.network.ServicesHelper;
import hust.nursenfcclient.patient.BedInfoActivity;
import hust.nursenfcclient.patient.PatientInfoItem;
import hust.nursenfcclient.patient.TemperInfoItem;

/**
 * Created by admin on 2015/12/4.
 */
public class PairedFragment extends Fragment implements View.OnClickListener{
    private LineChart mTemperChart;
    private List<TemperInfoItem> temperDatalist;

    private View lastTemperPopView;
    private PopupWindow lastTemperPopupWindow;

    private TextView lastTemperText;
    private View patientInfoHeader;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.patient_info_layout, null);

        // 获取来自Activity传递来的值
        TemperInfoItem temper_item = getData();
        // 先将本次的测量信息插入到数据库TemperInfo中
        DatabaseQueryHelper databaseQueryHelper = DatabaseQueryHelper.getInstance(getActivity().getApplicationContext());
        databaseQueryHelper.insertIntoTemperInfo(temper_item);

        PatientInfoItem item = databaseQueryHelper.getPatientInfoUseTagId(temper_item.getTag_id());
        // === 从数据库中查询温度测量信息 ===== //
        temperDatalist = DatabaseQueryHelper.getInstance(getActivity().getApplicationContext()).getTemperInfoFromDb(item.getTag_id());

        patientInfoHeader = $(view, R.id.patientInfoHeader);
        patientInfoHeader.setOnClickListener(this);
        // 初始化基本信息
        initPopupPatientInfo(view, item);

        // 表格控件使用
        mTemperChart = $(view, R.id.mTemperChart);

        // === 测试温度表 ====
        LineData data = ChartHelper.getData(temperDatalist, BedInfoActivity.MIN_TEMPER, BedInfoActivity.MAX_TEMPER, getActivity());
        ChartHelper.setupChart(mTemperChart, data);

        popUpLastTemper(temperDatalist.get(temperDatalist.size() - 1).getTemper_num());

        return view;
    }

    private TemperInfoItem getData() {
        TemperInfoItem temper_item = new TemperInfoItem();

        Bundle datas = getArguments();
        try {
            temper_item.setTag_id(datas.getString(ServicesHelper.TAG_ID));
            temper_item.setTemper_num(datas.getFloat(ServicesHelper.TEMPER_NUM, 37.5694f));
            temper_item.setNurse_id(datas.getString(ServicesHelper.NURSE_ID));
            temper_item.setLast_time(datas.getString(ServicesHelper.LAST_TIME));
            temper_item.setNext_time(datas.getString(ServicesHelper.NEXT_TIME));

        } catch (Exception e) {
            Log.e("LOG_TAG", "UnPairFragment getData Error:" + e.toString());
        }
        return temper_item;
    }

    // ===== 弹出本次测量温度信息 ===== //
    private void popUpLastTemper(float lastTemper) {
        DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String temper = decimalFormat.format(lastTemper);//format 返回的是字符串

        lastTemperPopView = LayoutInflater.from(getActivity()).inflate(R.layout.temper_textview, null);
        TextView temperTxt = (TextView)lastTemperPopView.findViewById(R.id.temperTxt);
        temperTxt.setText(temper + " 度");

        // === 定义动画效果 ===== //
        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1);
        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1);
        PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("rotation", 0, -3, -3, 3, -3, 3, -3, 3, -3, 0);
        final ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(temperTxt, pvhX, pvhY, pvhZ);
        animator.setDuration(2000);


        PropertyValuesHolder pvhX1 = PropertyValuesHolder.ofFloat("scaleX", 0, 1);
        PropertyValuesHolder pvhY2 = PropertyValuesHolder.ofFloat("scaleY", 0, 1);
        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(temperTxt, pvhX1, pvhY2);
        scaleAnimator.setDuration(1000).start();
        scaleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animator.start();
            }
        });

        final  ObjectAnimator disAnimator = ObjectAnimator.ofFloat(lastTemperPopView, "alpha", 1.0f, 0.0f);
        disAnimator.setDuration(3000);
        disAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                try {
                    if (lastTemperPopupWindow != null)
                        lastTemperPopupWindow.dismiss();
                } catch(Exception e) {}

            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                try {
                    if (disAnimator != null)
                        disAnimator.start();
                } catch (Exception e){}
            }
        });

        lastTemperPopupWindow = new PopupWindow(lastTemperPopView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        lastTemperPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        lastTemperPopupWindow.setOutsideTouchable(true);

        lastTemperPopupWindow.showAtLocation(lastTemperPopView, Gravity.CENTER, 0, 0);
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
        lastTemperText    = $ (popView, R.id.lastTemperText);
        ImageView patientPhotoImg  = $(popView, R.id.patientPhotoImg);

        // 设置界面
        try {
            bedIdText.setText(item.getBedId());
            bedStateText.setText(item.getBedState());
            patientIdText.setText(item.getPatientId());
            patientNameText.setText(item.getPatientName());
            patientAgeText.setText(String.valueOf(item.getPatientAge()));
            patientRecordText.setText(item.getPatientRecord());
            lastTemperText.setText(String.valueOf(temperDatalist.get(temperDatalist.size() - 1).getTemper_num() + "度"));


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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.patientInfoHeader:
                popUpLastTemper(temperDatalist.get(temperDatalist.size() - 1).getTemper_num());
                break;
        }
    }

    private <T> T $(View view, int resId) {
        return (T)view.findViewById(resId);
    }
}
