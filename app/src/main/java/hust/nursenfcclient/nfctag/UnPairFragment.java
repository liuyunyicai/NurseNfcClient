package hust.nursenfcclient.nfctag;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;
import java.util.List;

import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.database.DatabaseQueryHelper;
import hust.nursenfcclient.network.ServicesHelper;
import hust.nursenfcclient.patient.PatientInfoItem;
import hust.nursenfcclient.patient.TemperInfoItem;

/**
 * Created by admin on 2015/12/4.
 */
public class UnPairFragment extends Fragment implements TextWatcher {

    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<PatientInfoItem> dataLists;

    private List<PatientInfoItem> Alldata_Lists;

    private SharedPreferences sharedPreferences;

    private AutoCompleteTextView searchEdit;
    private ImageButton searchDeleteBt;

    private ArrayAdapter<String> houseIdsAdapter;
    private List<String> houseIdsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.listview, container, false);

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.SHAREDPR_NAME, Context.MODE_PRIVATE);

        mListView = (ListView) view.findViewById(R.id.listview);
        searchEdit = (AutoCompleteTextView) view.findViewById(R.id.searchEdit);
        searchDeleteBt = (ImageButton) view.findViewById(R.id.searchDeleteBt);

        searchEdit.addTextChangedListener(this);

        searchDeleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText("");
            }
        });

        // 获取来自Activity传递来的值
        TemperInfoItem item = getData();

        // ==== 获取未匹配数据 =====//
        Alldata_Lists = DatabaseQueryHelper.getInstance(getActivity().getApplicationContext()).getUnpairPatientFromDb();
        dataLists = new ArrayList<>(Alldata_Lists);

        // 设置提醒
        houseIdsList = getHouseIdsFromAllDataList();
        houseIdsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, houseIdsList);
        searchEdit.setAdapter(houseIdsAdapter);
        if (houseIdsList != null && houseIdsList.size() > 0) {
            searchEdit.setText(houseIdsList.get(0).substring(0, 1));
            searchEdit.requestFocus();
        }


        mAdapter = new ListViewAdapter(getActivity(), dataLists, item);
        mListView.setAdapter(mAdapter);
        mAdapter.setMode(Attributes.Mode.Single);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
            }
        });

        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f, 0.0f, 1.0f);
        animator.setDuration(1000).start();

        return view;
    }

    // 获取所有未匹配病人列表中的病床号
    private List<String> getHouseIdsFromAllDataList() {
        List<String> housIds = new ArrayList<>();
        try {
            if (Alldata_Lists != null) {
                for (PatientInfoItem item : Alldata_Lists) {
                    String temp_housId = item.getHouseId();
                    if (!housIds.contains(temp_housId)) {
                        housIds.add(temp_housId);
                    }
                }
            }

        } catch (Exception e) {
        }
        return housIds;
    }

    private TemperInfoItem getData() {
        TemperInfoItem item = new TemperInfoItem();

        Bundle datas = getArguments();
        try {
            item.setTag_id(datas.getString(ServicesHelper.TAG_ID));
            item.setTemper_num(datas.getFloat(ServicesHelper.TEMPER_NUM, 38.5694f));
            item.setNurse_id(datas.getString(ServicesHelper.NURSE_ID));

        } catch (Exception e) {
            Log.e("LOG_TAG", "UnPairFragment getData Error:" + e.toString());
        }
        return item;
    }

    // 监听EditText文本变化
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            dataLists.clear();
            for (PatientInfoItem item : Alldata_Lists) {
                if (item.getBedId().contains(s))
                    dataLists.add(item);
            }
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {}
    }
}
