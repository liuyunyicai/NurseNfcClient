package hust.nursenfcclient.nfctag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.helps.NFC_Helper;
import hust.nursenfcclient.network.ServicesHelper;
import hust.nursenfcclient.patient.PatientInfoItem;
import hust.nursenfcclient.patient.TemperInfoItem;

/**
 * Created by admin on 2015/12/4.
 */
public class NFCSearchingFragment extends Fragment implements View.OnClickListener {
    private Button getPairedTagBt, getUnpairTagBt;
    private TextView searchingTxt;

    private String[] searching_texts;

    // ==== 资源 ==== //
    private String searching_failed_text, searching_complete_text;
    private int red, black, gray;
    private SharedPreferences sharedPreferences;

    //NFC相关信息
    private NFC_Helper nfc_helper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nfc_searching_layout, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.SHAREDPR_NAME, Context.MODE_PRIVATE);
        nfc_helper = NFC_Helper.getInstance(getActivity().getApplicationContext());

        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Resources res = getResources();

        searching_texts = res.getStringArray(R.array.searching_texts);
        searching_failed_text = res.getString(R.string.searching_failed_text);
        searching_complete_text = res.getString(R.string.searching_complete_text);

        red = res.getColor(R.color.red);
        black = res.getColor(R.color.black);
        gray = res.getColor(R.color.gray);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView(View view) {
        getPairedTagBt = $(view, R.id.getPairedTagBt);
        getUnpairTagBt = $(view, R.id.getUnpairTagBt);
        searchingTxt = $(view, R.id.searchingTxt);

//        getUnpairTagBt.setOnClickListener(this);
//        getPairedTagBt.setOnClickListener(this);

        getUnpairTagBt.setVisibility(View.GONE);
        getPairedTagBt.setVisibility(View.GONE);

        // 测试扫描NFC场景
//        new TestSearchingTask().execute();
    }



    // 显示NFC扫描结果
    public void showEndSearchingResult(boolean isSuccess, final NfcTagInfoItem tagInfoItem) {
        if (isSuccess) {
            searchingTxt.setText(searching_complete_text);
            searchingTxt.setTextColor(red);
        } else {
            searchingTxt.setText(searching_failed_text);
            searchingTxt.setTextColor(black);
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(searchingTxt, "alpha", 1.0f, 0.0f);
        animator.setDuration(1000);
        animator.start();

        // 设置界面消失效果
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (searchingTxt != null)
                    searchingTxt.setVisibility(View.GONE);
                // 提醒NFCSearchActivity来更新界面
                Log.w(MainActivity.LOG_TAG, "searchingTxt Gone");
                EventBus.getDefault().post(new NFCSearchEvent(NFCSearchEvent.SHOW_PIAR_OR_UNPAIR_WINDOW));
            }
        });
    }



    private <T> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getPairedTagBt:
                break;
            case R.id.getUnpairTagBt:
                break;
        }
    }
}

