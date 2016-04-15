package hust.nursenfcclient.house;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import hust.nursenfcclient.R;

/**
 * Created by admin on 2015/11/23.
 */
public class HousInfoAdapter extends BaseAdapter{
    private List<HouseInfoItem> dataLists;
    private LayoutInflater inflater;
    private ViewHolder holder;

    public HousInfoAdapter(List<HouseInfoItem> dataLists, Context context) {
        this.dataLists = dataLists;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        if (dataLists != null)
            return dataLists.size();
        return 0;
    }

    @Override
    public HouseInfoItem getItem(int position) {
        if ((dataLists != null) && (dataLists.size() != 0))
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
            convertView = inflater.inflate(R.layout.house_item_layout, parent, false);
            holder = new ViewHolder();
            holder.houseIdText = (TextView) convertView.findViewById(R.id.houseIdText);
            holder.houseRemainNum = (TextView) convertView.findViewById(R.id.houseRemainNum);
            holder.houseIcfoPrBar = (ProgressBar) convertView.findViewById(R.id.houseIcfoPrBar);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        try {
            HouseInfoItem item = getItem(position);
            holder.houseIdText.setText(item.getHouseId());
            holder.houseIcfoPrBar.setProgress((int) item.getCheckedPrecent());
            holder.houseRemainNum.setText(item.getBedCheckedNum() + File.separator + item.getBedAllNum() + "人");
        } catch (Exception e) {
            Log.i("LOG_TAG", e.toString());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView houseIdText;
        TextView houseRemainNum;
        ProgressBar houseIcfoPrBar;
    }
}
