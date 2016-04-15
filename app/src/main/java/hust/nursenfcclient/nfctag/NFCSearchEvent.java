package hust.nursenfcclient.nfctag;

import hust.nursenfcclient.patient.TemperInfoItem;

public class NFCSearchEvent {
    public static final int PAIR_SUCCESS_EVENT = 1; // 匹配成功
    public static final int PAIR_FAILED_EVENT  = 2;  // 匹配失败
    public static final int POPUP_PAIRED_WINDOW_EVENT = 3; // 打开已经匹配成功情况下的弹出框
    public static final int POPUP_UNPAIR_WINDOW_EVENT = 4; // 打开未匹配情况下的弹出框
    public static final int CLOSE_EVENT = 5; // 关闭Activity

    public static final int READ_TEMPER = 10; //点击按钮后，读取温度

    public static final int TEST_CLICK_BT = 7; // 测试，按钮进行扫描

    public static final int SHOW_PIAR_OR_UNPAIR_WINDOW = 6; // 关闭NFCSearchFragment，切换到成功或者关闭界面

    private int action;
    private NfcTagInfoItem dataItem;


    public NFCSearchEvent(int action) {
        this.action = action;
    }

    public NfcTagInfoItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(NfcTagInfoItem dataItem) {
        this.dataItem = dataItem;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}

