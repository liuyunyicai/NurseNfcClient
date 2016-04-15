package hust.nursenfcclient.nfctag;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.helps.NFC_Helper;

/**
 * Created by admin on 2015/12/3.
 *
 * 用以避免多次读取失败回退，使用的测试版扫描标签Fragment
 */
public class TestSearchingFragment extends Fragment implements View.OnClickListener {
    private Button getPairedTagBt, getUnpairTagBt;
    private TextView searchingTxt;

    private String[] searching_texts;

    // ==== 资源 ==== //
    private View view;
    private String searching_failed_text, searching_complete_text, nfc_onclick_hint;
    private int red, black, gray, w_gray, w_write;
    private SharedPreferences sharedPreferences;

    //NFC相关信息
    private NFC_Helper nfc_helper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.nfc_searching_layout, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.SHAREDPR_NAME, Context.MODE_PRIVATE);
        nfc_helper = NFC_Helper.getInstance(getActivity().getApplicationContext());

//        Toast.makeText(getActivity(), "TestSearchingFragment", Toast.LENGTH_SHORT).show();
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
        nfc_onclick_hint = res.getString(R.string.nfc_onclick_hint);

        red = res.getColor(R.color.red);
        black = res.getColor(R.color.black);
        gray = res.getColor(R.color.gray);
        w_gray = res.getColor(R.color.w_gray);
        w_write = res.getColor(R.color.w_write);
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
        searchingTxt.setOnClickListener(this);

        // 测试扫描NFC场景
//        new TestSearchingTask().execute();
    }

    // 正在写入数据时，将背景设置为灰色
    public void onWritingData() {
        if (view != null)
            view.setBackgroundColor(w_gray);
    }

    // 写入数据完成后，背景设置为白色（亮色）
    public void onDataWrited() {
        if (view != null)
            view.setBackgroundColor(w_write);
    }

    // 显示OnClickButton效果
    public void onShowOnClickButton() {
        searchingTxt.setText(nfc_onclick_hint);
        searchingTxt.setTextColor(red);
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
            case R.id.searchingTxt:
                // 点击发送读取TAG中温度信息的消息
                EventBus.getDefault().post(new NFCSearchEvent(NFCSearchEvent.READ_TEMPER));
                break;
        }
    }
}
