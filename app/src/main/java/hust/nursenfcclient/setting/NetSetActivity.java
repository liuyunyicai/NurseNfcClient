package hust.nursenfcclient.setting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.helps.WifiHelper;

/**
 * Created by admin on 2015/11/20.
 */
public class NetSetActivity extends Activity implements View.OnClickListener {

    private ImageView returnBt;
    private ListView setnetListView;
    private List<NetSetItem> dataLists;
    private NetSetAdapter adapter;
    private PopupWindow setIPPopupWindow;
    private View popView;

    private SharedPreferences sharedPreferences;

    // 每个设置按钮的标志
    private static final int WIFI_SET_POS     = 0;
    private static final int NFC_SET_POS      = 1;
    private static final int USB_SET_POS      = 2;
    private static final int BLUETOOH_SET_POS = 3;
    private static final int ALARM_SET_POS    = 4;
    private static final int NET_SET_POS      = 5;

    // 滑动距离
    private float mTouchSlop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netset_layout);
        sharedPreferences = getSharedPreferences(MainActivity.SHAREDPR_NAME, MODE_PRIVATE);
        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        initView();
    }

    private void initView() {
        returnBt = $(R.id.returnBt);
        returnBt.setOnClickListener(this);

        setnetListView = $(R.id.setnetListView);
        dataLists = new ArrayList<>();

        dataLists.add(new NetSetItem(R.mipmap.set_wifi_icon, R.string.wifi_set, !WifiHelper.isWifiConnected(this)));
        dataLists.add(new NetSetItem(R.mipmap.set_nfc_icon, R.string.nfc_set, false));
        dataLists.add(new NetSetItem(R.mipmap.set_usb_icon, R.string.usb_set, true));
        dataLists.add(new NetSetItem(R.mipmap.set_bluetooth_icon, R.string.bluetooth_set, true));
        dataLists.add(new NetSetItem(R.mipmap.set_alarm_icon, R.string.alarm_set, true));
        dataLists.add(new NetSetItem(R.mipmap.set_net_icon, R.string.netset_text, true));

        adapter = new NetSetAdapter(this, dataLists);
        setnetListView.setAdapter(adapter);

        setnetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 改变状态
                dataLists.get(position).changeIsClosed();
                switch (position) {
                    case WIFI_SET_POS:
                        onSetWifi();
                        break;
                    case NFC_SET_POS:
                        onSetNfc();
                        break;
                    case USB_SET_POS:
                        onSetUsb();
                        break;
                    case BLUETOOH_SET_POS:
                        onSetBluetooh();
                        break;
                    case ALARM_SET_POS:
                        onSetAlarm();
                        break;
                    case NET_SET_POS:
                        onSetNet(view);
                        break;
                }
                adapter.notifyDataSetChanged();
            }
        });

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
                NetSetActivity.this.finish();
                break;
        }
    }

    //======= 设置按钮响应函数 ====== //
    private void onSetWifi() {
        WifiHelper.reverseWifiState(this);
    }

    private void onSetNfc() {}
    private void onSetUsb() {}
    private void onSetBluetooh() {}
    private void onSetAlarm() {}


    private void onSetNet(View view) {
        if (dataLists.get(NET_SET_POS).isClosed()) {
            closeNetPopWindow();
        } else {
            try {
                if (popView != null)
                    setnetListView.removeFooterView(popView);
            } catch(Exception e) {}

            if (popView == null)
                popView = LayoutInflater.from(this).inflate(R.layout.set_ip_layout, null);

            setnetListView.addFooterView(popView);

            setNetInitView();

            ObjectAnimator animator = ObjectAnimator.ofFloat(popView, "alpha", 0.0f, 1.0f);
            animator.setDuration(500);
            animator.start();
        }
    }

    private void closeNetPopWindow() {
        if (popView != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(popView , "alpha", 1.0f, 0.0f);
            animator.setDuration(500);
            animator.start();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
        }
    }

    private void setNetInitView() {
        if (popView != null) {
            String server_ip = sharedPreferences.getString(MainActivity.SHARED_SERVER_IP, MainActivity.DEFAULT_SERVER_IP);
            String server_port = sharedPreferences.getString(MainActivity.SHARED_SERVER_PORT, MainActivity.DEFAULT_SERVER_PORT);
            String[] ips = server_ip.split("\\.");

            final EditText ipEdit1 = $(popView, R.id.ipEdit1);
            final EditText ipEdit2 = $(popView, R.id.ipEdit2);
            final EditText ipEdit3 = $(popView, R.id.ipEdit3);
            final EditText ipEdit4 = $(popView, R.id.ipEdit4);
            final EditText portEdit = $(popView, R.id.portEdit);
            final Button setnetEnsureBt = $(popView, R.id.setnetEnsureBt);

            try {
                Log.w("LOG_TAG", "IP length" + ips.length);

                if ((ips != null) && (ips.length == 4)) {
                    ipEdit1.setText(ips[0]);
                    ipEdit2.setText(ips[1]);
                    ipEdit3.setText(ips[2]);
                    ipEdit4.setText(ips[3]);
                }

                if (server_port != null)
                    portEdit.setText(server_port);

                setnetEnsureBt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] temp_ips = new String[4];
                        String temp_port = "";

                        temp_ips[0] = ipEdit1.getText().toString().trim();
                        temp_ips[1] = ipEdit2.getText().toString().trim();
                        temp_ips[2] = ipEdit3.getText().toString().trim();
                        temp_ips[3] = ipEdit4.getText().toString().trim();
                        temp_port   = portEdit.getText().toString().trim();

                        if ((strIsValid(temp_ips[0])) &&
                            (strIsValid(temp_ips[1])) &&
                            (strIsValid(temp_ips[2])) && 
                            (strIsValid(temp_ips[3])) &&
                            (strIsValid(temp_port))) {
                            String tempIp = temp_ips[0] + "." +
                                            temp_ips[1] + "." +
                                            temp_ips[2] + "." +
                                            temp_ips[3] ;

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(MainActivity.SHARED_SERVER_IP, tempIp);
                            editor.putString(MainActivity.SHARED_SERVER_PORT, temp_port);
                            editor.commit();

                            dataLists.get(NET_SET_POS).setIsClosed(true);
                            closeNetPopWindow();
                            if (adapter != null)
                                adapter.notifyDataSetChanged();
                            Toast.makeText(NetSetActivity.this, R.string.change_net_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NetSetActivity.this, R.string.empty_input_hint, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (Exception e) {
                Log.e("LOG_TAG", "网络设置出错 " + e.toString());
            }
        }
    }

    private boolean strIsValid(String str) {
        return (str != null) && (!str.equals(""));
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
                    NetSetActivity.this.finish();
                }
                break;
        }
        return true;
    }

}
