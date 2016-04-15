package hust.nursenfcclient.patient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hust.nursenfcclient.R;
import hust.nursenfcclient.imageloader.ImageHelper;

/**
 * Created by admin on 2015/11/23.
 */
public class PatientInfoAdapter extends BaseAdapter {
    private List<PatientInfoItem> dataLists;
    private LayoutInflater inflater;
    private ViewHolder holder;
    private Context mContext;

    public PatientInfoAdapter(List<PatientInfoItem> dataLists, Context context) {
        this.dataLists = dataLists;
        inflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        if (dataLists != null)
            return dataLists.size();
        return 0;
    }

    @Override
    public PatientInfoItem getItem(int position) {
        if (dataLists != null)
            return dataLists.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bed_item_layout, parent, false);
            holder = new ViewHolder();
            holder.bedIdText = $(convertView, R.id.bedIdText);
            holder.bedStateText = $(convertView, R.id.bedStateText);
            holder.patientIdText = $(convertView, R.id.patientIdText);
            holder.patientNameText = $(convertView, R.id.patientNameText);
            holder.patientAgeText = $(convertView, R.id.patientAgeText);
            holder.patientGenderImg = $(convertView, R.id.patientGenderImg);
            holder.patientRecordText = $(convertView, R.id.patientRecordText);
            holder.lastTemperText = $(convertView, R.id.lastTemperText);
            holder.patientPhotoImg = $(convertView, R.id.patientPhotoImg);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // 设置界面
        try {
            PatientInfoItem item = getItem(position);
            holder.bedIdText.setText(item.getBedId());
            holder.bedStateText.setText(item.getBedState());
            holder.patientIdText.setText(item.getPatientId());
            holder.patientNameText.setText(item.getPatientName());
            holder.patientAgeText.setText(String.valueOf(item.getPatientAge()));
            holder.patientRecordText.setText(item.getPatientRecord());
            holder.lastTemperText.setText(String.valueOf(item.getLastTemper()) + "度");


            if (item.isChecked())
                holder.bedStateText.setTextColor(mContext.getResources().getColor(R.color.gray));
            else
                holder.bedStateText.setTextColor(mContext.getResources().getColor(R.color.red));

            if (item.getPatientGender())
                holder.patientGenderImg.setImageResource(R.mipmap.male_icon);
            else
                holder.patientGenderImg.setImageResource(R.mipmap.femal_icon);

//            ImageHelper.setImageFitXY(holder.patientPhotoImg, item.getPatientPhoto(), 240 , 360);
            ImageHelper.setImageRaw(holder.patientPhotoImg, item.getPatientPhoto());

        } catch (Exception e) {
            Log.i("LOG_TAG", "PatientInfoAdapter 加载数据出现错误：" + e.toString());
        }

        return convertView;
    }

    private <T> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    static class ViewHolder {
        TextView bedIdText;
        TextView bedStateText;
        TextView patientIdText;
        TextView patientNameText;
        TextView patientAgeText;
        ImageView patientGenderImg;
        TextView patientRecordText;
        TextView lastTemperText;
        ImageView patientPhotoImg;
    }

}
