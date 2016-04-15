package hust.nursenfcclient.nfctag;

import hust.nursenfcclient.patient.TemperInfoItem;

/**
 * Created by admin on 2015/12/4.
 */
public class NFCEvent {
    public static final int PAIR_SUCCESS_EVENT = 1; // 匹配成功
    public static final int PAIR_FAILED_EVENT  = 2;  // 匹配失败
    public static final int POPUP_PAIRED_WINDOW_EVENT = 3; // 打开已经匹配成功情况下的弹出框
    public static final int POPUP_UNPAIR_WINDOW_EVENT = 4; // 打开未匹配情况下的弹出框
    public static final int CLOSE_EVENT = 5; // 关闭Activity

    private int action;
    private TemperInfoItem dataItem;

    public NFCEvent(int action, TemperInfoItem dataItem) {
        this.action = action;
        this.dataItem = dataItem;
    }

    public TemperInfoItem getDataItem() {
        return dataItem;
    }

    public void setDataItem(TemperInfoItem dataItem) {
        this.dataItem = dataItem;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
