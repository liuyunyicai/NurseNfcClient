package hust.nursenfcclient.patient;

import android.widget.TextView;

/**
 * Created by admin on 2015/12/2.
 */
public class TemperInfoItem {
    private String tag_id;
    private String nurse_id;
    private float temper_num;
    private String last_time;
    private String next_time;

    public TemperInfoItem() {}

    public TemperInfoItem(String tag_id, String nurse_id, float temper_num, String last_time, String next_time) {
        this.tag_id = tag_id;
        this.nurse_id = nurse_id;
        this.temper_num = temper_num;
        this.last_time = last_time;
        this.next_time = next_time;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getNurse_id() {
        return nurse_id;
    }

    public void setNurse_id(String nurse_id) {
        this.nurse_id = nurse_id;
    }

    public float getTemper_num() {
        return temper_num;
    }

    public void setTemper_num(float temper_num) {
        this.temper_num = temper_num;
    }

    public String getLast_time() {
        return last_time;
    }

    public void setLast_time(String last_time) {
        this.last_time = last_time;
    }

    public String getNext_time() {
        return next_time;
    }

    public void setNext_time(String next_time) {
        this.next_time = next_time;
    }
}
