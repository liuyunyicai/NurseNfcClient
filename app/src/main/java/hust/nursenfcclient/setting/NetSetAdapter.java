package hust.nursenfcclient.setting;

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
import hust.nursenfcclient.init.MySwitchButton;

/**
 * Created by admin on 2015/11/20.
 */
public class NetSetAdapter extends BaseAdapter {

    private List<NetSetItem> dataLists;
    private LayoutInflater inflater;
    private ViewHolder holder;

    public NetSetAdapter(Context context, List<NetSetItem> dataLists) {
        inflater = LayoutInflater.from(context);
        this.dataLists = dataLists;
    }

    @Override
    public int getCount() {
        if (dataLists != null)
            return dataLists.size();
        return 0;
    }

    @Override
    public NetSetItem getItem(int position) {
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
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.setnet_item_layout, parent, false);

            holder.setnetIcon = $(convertView, R.id.setnetIcon);
            holder.setnetText = $(convertView, R.id.setnetText);
            holder.stateSwitch = $(convertView, R.id.stateSwitch);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NetSetItem item = getItem(position);
        try {
            holder.setnetIcon.setImageResource(item.getIconResId());
            holder.setnetText.setText(item.getTextResId());
            holder.stateSwitch.setBackGround(item.isClosed());

        } catch (Exception e) {
            Log.e("LOG_TAG", e.toString());
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView setnetIcon;
        TextView setnetText;
        MySwitchButton stateSwitch;
    }

    private <T> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }
}
