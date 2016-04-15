package hust.nursenfcclient.patient;

import hust.nursenfcclient.house.HouseInfoItem;

/**
 * Created by admin on 2015/12/1.
 */

/** 用以MainActivity与BedInfoActivity通过EventBus传递事件的Event类 **/
public class BedInfoEvent {

    private HouseInfoItem mItem;

    public BedInfoEvent(HouseInfoItem item) {
        mItem = item;
    }

    public HouseInfoItem getmItem() {
        return mItem;
    }

    public void setmItem(HouseInfoItem mItem) {
        this.mItem = mItem;
    }
}
