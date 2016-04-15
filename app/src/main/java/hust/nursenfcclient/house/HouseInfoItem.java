package hust.nursenfcclient.house;

import java.util.List;

/**
 * Created by admin on 2015/11/23.
 */

// House基本信息
public class HouseInfoItem {
    private boolean isFloorFirst = true;
    private String floorId; // House 的 Floor ID号
    private String houseId; // House 的 ID号
    private int houseState; // House 的状态
    private int bedAllNum;  // bed的总数
    private int bedCheckedNum;  // bed的剩余数量
    private float checkedPrecent; // checked的总数量（*100后）

    private List<String> bed_ids;

    // 默认值
    private static final String DEFAULT_FLOORID = "A栋1楼";

    public static final int UNCHECK = -1;
    public static final int EMPTY   = 0;
    public static final int CHECKED = 1;

    public static final String UNCHECK_STR = "uncheck";
    public static final String EMPTY_STR = "empty";
    public static final String CHECKED_STR = "checked";

    public HouseInfoItem (boolean isFloorFirst, String floorId, String houseId, int houseState, float checkedPrecent) {
        this.isFloorFirst = isFloorFirst;
        this.houseId = houseId;
        this.floorId = floorId;

        this.houseState = houseState;
        this.checkedPrecent = checkedPrecent;
    }

    public HouseInfoItem(String houseId, int houseState, int bedAllNum, int bedCheckedNum) {
        this.houseId = houseId;
        this.houseState = houseState;
        this.bedAllNum = bedAllNum;
        this.bedCheckedNum = bedCheckedNum;
        resetCheckedPrecent();
        this.floorId = DEFAULT_FLOORID;
        isFloorFirst = true;
    }

    public HouseInfoItem() {

    }

    public int getBedAllNum() {
        return bedAllNum;
    }

    public void setBedAllNum(int bedAllNum) {
        this.bedAllNum = bedAllNum;
    }

    public int getBedCheckedNum() {
        return bedCheckedNum;
    }

    public void setBedCheckedNum(int bedCheckedNum) {
        this.bedCheckedNum = bedCheckedNum;
    }

    public boolean isFloorFirst() {
        return isFloorFirst;
    }

    public void setIsFloorFirst(boolean isFloorFirst) {
        this.isFloorFirst = isFloorFirst;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public int getHouseState() {
        return houseState;
    }

    public void setHouseState(int houseState) {
        this.houseState = houseState;
    }

    public void setHouseState(String houseStateStr) {
        switch (houseStateStr) {
            case UNCHECK_STR:
                houseState = UNCHECK;
                break;
            case EMPTY_STR:
                houseState = EMPTY;
                break;
            case CHECKED_STR:
                houseState = CHECKED;
                break;
        }
    }

    public float getCheckedPrecent() {
        return checkedPrecent;
    }

    public void setCheckedPrecent(float checkedPrecent) {
        this.checkedPrecent = checkedPrecent;
    }

    public void resetCheckedPrecent() {
        checkedPrecent = (int)(100 * ((double) bedCheckedNum / bedAllNum));
    }

    public String getFloorId() {
        return floorId;
    }

    public void setFloorId(String floorId) {
        this.floorId = floorId;
    }

    public List<String> getBed_ids() {
        return bed_ids;
    }

    public void setBed_ids(List<String> bed_ids) {
        this.bed_ids = bed_ids;
    }
}
