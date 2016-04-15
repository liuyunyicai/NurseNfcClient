package hust.nursenfcclient.init;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.alarm.AlarmEvent;
import hust.nursenfcclient.database.NurseNFCDatabaseHelper;
import hust.nursenfcclient.imageloader.ImageHelper;
import hust.nursenfcclient.network.ServicesHelper;

/**
 * Created by admin on 2015/11/21.
 */
public class CheckInActivity extends BaseActivity implements View.OnClickListener{
    private TextView nurseNameText, nurseGenderText, nurseAgeText, nurseProjectText, nurseIdText;
    private ImageView nurseIdPhoto;
    private Button errorBt, correctBt;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkin_layout);
        sharedPreferences = getSharedPreferences(MainActivity.SHAREDPR_NAME, MODE_PRIVATE);

        initView();
        initData();
    }

    private void initView() {
        nurseNameText = $(R.id.nurseNameText);
        nurseGenderText = $(R.id.nurseGenderText);
        nurseAgeText = $(R.id.nurseAgeText);
        nurseProjectText = $(R.id.nurseProjectText);
        nurseIdText = $(R.id.nurseIdText);
        nurseIdPhoto = $(R.id.nurseIdPhoto);
        errorBt = $(R.id.errorBt);
        correctBt = $(R.id.correctBt);

        errorBt.setOnClickListener(this);
        correctBt.setOnClickListener(this);
    }

    // 初始化数据
    private void initData() {
        NurseNFCDatabaseHelper nurseDbHelper = NurseNFCDatabaseHelper.getInstance(getApplicationContext());

        JSONObject dataObject = new JSONObject();
        try {
            nurseDbHelper.getAllDataFromTable(dataObject, ServicesHelper.NURSE_INFO_TABLE_NAME);
            JSONArray dataArray = dataObject.getJSONArray(ServicesHelper.NURSE_INFO_TABLE_NAME);

            JSONObject temp_dataObj = dataArray.getJSONObject(0);

            String nurseName = temp_dataObj.getString(ServicesHelper.NURSE_NAME);
            String nurseId = temp_dataObj.getString(ServicesHelper.NURSE_ID);
            String nursePhotoUri = temp_dataObj.getString(ServicesHelper.NURSE_PHOTO);

            nurseNameText.setText(nurseName);
            nurseIdText.setText(nurseId);

            // 将获得的值保存下来
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(ServicesHelper.NURSE_ID, nurseId);
            editor.putString(ServicesHelper.NURSE_NAME, nurseName);

            // 加载头像
            if ((nursePhotoUri != null) && (!nursePhotoUri.equals(""))) {
                ImageHelper.setImageFitXY(nurseIdPhoto, nursePhotoUri);
                editor.putString(MainActivity.SHARED_NURSE_PHOTOURI, nursePhotoUri);
            }

            editor.commit();
        } catch (JSONException e) {
            Log.i("LOG_TAG", e.toString());
        }
    }

    private <T> T $(int resId) {
        return (T) findViewById(resId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.errorBt:
                startActivity(new Intent(this, LogInActivity.class));
                this.finish();
                break;
            case R.id.correctBt:
                startActivity(new Intent(this, MainActivity.class));

                // 同时注意开启提醒功能
                EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_START_ALARM));
                // 开启数据库维护线程
                EventBus.getDefault().post(new AlarmEvent(AlarmEvent.ACTION_STRAT_UPDATE_DB));
                this.finish();
                break;
        }
    }
}
