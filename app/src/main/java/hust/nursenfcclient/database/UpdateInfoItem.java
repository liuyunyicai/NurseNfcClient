package hust.nursenfcclient.database;

/**
 * Created by admin on 2015/12/5.
 */
public class UpdateInfoItem {
    private String tag_id;
    private String patient_id;
    private String bed_id;
    private String house_id;
    private String house_state;
    private String bed_state;
    private String next_time;
    private boolean isNeedCheck;

    public UpdateInfoItem() {}

    public String getBed_id() {
        return bed_id;
    }

    public void setBed_id(String bed_id) {
        this.bed_id = bed_id;
    }

    public String getBed_state() {
        return bed_state;
    }

    public void setBed_state(String bed_state) {
        this.bed_state = bed_state;
    }

    public String getHouse_id() {
        return house_id;
    }

    public void setHouse_id(String house_id) {
        this.house_id = house_id;
    }

    public String getHouse_state() {
        return house_state;
    }

    public void setHouse_state(String house_state) {
        this.house_state = house_state;
    }

    public String getNext_time() {
        return next_time;
    }

    public void setNext_time(String next_time) {
        this.next_time = next_time;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(String patient_id) {
        this.patient_id = patient_id;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public boolean isNeedCheck() {
        return isNeedCheck;
    }

    public void setIsNeedCheck(boolean isNeedCheck) {
        this.isNeedCheck = isNeedCheck;
    }
}
