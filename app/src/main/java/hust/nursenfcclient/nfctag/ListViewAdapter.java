package hust.nursenfcclient.nfctag;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.util.List;

import de.greenrobot.event.EventBus;
import hust.nursenfcclient.R;
import hust.nursenfcclient.database.DatabaseQueryHelper;
import hust.nursenfcclient.imageloader.ImageHelper;
import hust.nursenfcclient.patient.PatientInfoItem;
import hust.nursenfcclient.patient.TemperInfoItem;

public class ListViewAdapter extends BaseSwipeAdapter {

    private Context mContext;
    private List<PatientInfoItem> dataLists;
    private TemperInfoItem temperInfoItem;

    public ListViewAdapter(Context mContext, List<PatientInfoItem> dataLists, TemperInfoItem temperInfoItem) {
        this.mContext = mContext;
        this.dataLists = dataLists;
        this.temperInfoItem = temperInfoItem;
    }

    public ListViewAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        final View v = LayoutInflater.from(mContext).inflate(R.layout.listview_item, null);
        SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.addSwipeListener(new SimpleSwipeListener() {
            @Override
            public void onOpen(SwipeLayout layout) {

                // === 定义动画效果 ===== //
                PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("scaleX", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1);
                PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("scaleY", 1, 0.9f, 0.9f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1.1f, 1);
                PropertyValuesHolder pvhZ = PropertyValuesHolder.ofFloat("rotation", 0, -3, -3, 3, -3, 3, -3, 3, -3, 0);
                ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(layout.findViewById(R.id.trash), pvhX, pvhY, pvhZ);
                animator.setDuration(500);
                animator.start();

            }
        });
        v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PatientInfoItem item = dataLists.get(position);

                    boolean isSuccess = DatabaseQueryHelper.getInstance(mContext.getApplicationContext())
                            .pairPatientAndTag(item.getPatientId(), temperInfoItem);

                    if (isSuccess) {
                        Toast.makeText(mContext, R.string.pair_success, Toast.LENGTH_SHORT).show();

                        // 匹配成功后弹出测量温度界面
                        EventBus.getDefault().post(new NFCSearchEvent(NFCSearchEvent.PAIR_SUCCESS_EVENT));

                    } else {
                        ((Button)v.findViewById(R.id.delete)).setText(R.string.pair_failed);
                    }
                } catch(Exception e) {
                    Log.e("LOG_TAG", "配对发生问题");
                }
            }
        });
        return v;
    }

    @Override
    public void fillValues(int position, View convertView) {
        ImageView patientPhotoImg = $(convertView, R.id.patientPhotoImg);
        ImageView patientGenderImg = $(convertView, R.id.patientGenderImg);
        TextView bedIdText = $(convertView, R.id.bedIdText);
        TextView patientIdText = $(convertView, R.id.patientIdText);
        TextView patientNameText = $(convertView, R.id.patientNameText);
        TextView patientAgeText = $(convertView, R.id.patientAgeText);

        if (dataLists != null) {
            try {
                PatientInfoItem item = dataLists.get(position);
                bedIdText.setText(item.getBedId());
                patientIdText.setText(item.getPatientId());
                patientNameText.setText(item.getPatientName());
                patientAgeText.setText(String.valueOf(item.getPatientAge()));

                if (item.getPatientGender())
                    patientGenderImg.setImageResource(R.mipmap.male_icon);
                else
                    patientGenderImg.setImageResource(R.mipmap.femal_icon);

                ImageHelper.setImageRaw(patientPhotoImg, item.getPatientPhoto());

            } catch (Exception e) {
                Log.i("LOG_TAG", "ListViewAdapter出错：" + e.toString());
            }
        }
    }

    private <T> T $(View view, int resId) {
        return (T) view.findViewById(resId);
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
            dataLists.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
