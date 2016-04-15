package hust.nursenfcclient.house;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import java.util.ArrayList;
import java.util.List;

import hust.nursenfcclient.MainActivity;
import hust.nursenfcclient.R;
import hust.nursenfcclient.database.DatabaseQueryHelper;
import hust.nursenfcclient.imageloader.ImageHelper;
import hust.nursenfcclient.network.ServicesHelper;
import hust.nursenfcclient.patient.BedInfoActivity;

/**
 * Created by admin on 2015/11/22.
 */
public class HouseInfoFragment extends Fragment implements View.OnClickListener{

    private SharedPreferences sharedPreferences;

    private HousInfoAdapter mAdapter;
    private PullToRefreshAnimationListView houseInfoList;
    private ListView actualListView;

    public static int curPos = 0;

    private float mTouchSlop;
    private boolean isHeadShow = true;
    private View emptyHeader, footerView;
    private RelativeLayout taskRemainView, headView;
    private ObjectAnimator outAnimator, inAnimator;
    private TextView taskRemainText, houseHeadText;
    private ProgressBar taskRemainProgress;
    private ImageView taskRemainPhoto;

    private float mFirstPosY, mCurrPosY;
    private float mLastY;
    private final static int EXTRA_NUM = 3;

    private int curDirection = DOWN;
    private static final int UP = 1;
    private static final int SCROLLING = 0;
    private static final int DOWN = -1;

    private List<HouseInfoItem> dataLists = new ArrayList<>();
    private String[] houseIds, floorIds;
    private int[] houseStates = {HouseInfoItem.UNCHECK, HouseInfoItem.CHECKED};

    // 总剩余任务相关参数
    private int[] taskCounts; // [0]是houseAll，[1]是houseRemain，[2]是bedAll， [3]是bedRemain

    // 屏幕密度
    private float density;
    private static final int TOOLBAR_HEIGHT = 65;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.house_info_layout, container, false);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        density = dm.density;

        emptyHeader = inflater.inflate(R.layout.house_info_header, container, false);
        emptyHeader.setLayoutParams(new ListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                (int) (TOOLBAR_HEIGHT * density)));

        sharedPreferences = getActivity().getSharedPreferences(MainActivity.SHAREDPR_NAME, Context.MODE_PRIVATE);

        initTaskHeaderView(contentView);
        initView(contentView);
//        initTestView(contentView);
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    // 初始化Task headerView
    private void initTaskHeaderView(View contentView) {
        headView = $(contentView, R.id.headView);
        taskRemainView = $(contentView, R.id.taskRemainView);
        taskRemainText = $(contentView, R.id.taskRemainText);
        taskRemainProgress = $(contentView, R.id.taskRemainProgress);
        taskRemainPhoto = $(contentView, R.id.taskRemainPhoto);
        taskCounts = DatabaseQueryHelper.getInstance(getActivity().getApplicationContext()).getRemainTaskFromDb();
        try {
            if ((taskCounts != null) && (taskCounts.length == 4)) {
                String taskString = "总共：" + taskCounts[2] + "人/" + taskCounts[0] + "房 剩余："
                        + taskCounts[3] + "人/" + taskCounts[1] + "房";

                taskRemainText.setText(taskString);
                taskRemainProgress.setProgress(100 - (int) ((double) taskCounts[3] / (double) taskCounts[2] * 100));

                String photo_uri = sharedPreferences.getString(ServicesHelper.NURSE_PHOTO, "");
                if ((photo_uri != null) && (!photo_uri.equals(""))) {
                    ImageHelper.setImageFitXY(taskRemainPhoto, photo_uri);
                }
            }
        } catch (Exception e) {}
    }


    private void initView(final View contentView) {
        // 默认的滑动距离
        mTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();

        houseInfoList = $(contentView, R.id.houseInfoList);
        houseHeadText = $(contentView, R.id.houseHeadText);

        // 添加OnRefreshListener
        houseInfoList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<AnimationListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<AnimationListView> refreshView) {
                String label = DateUtils.formatDateTime(getActivity().getApplicationContext(), System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                // Update the LastUpdatedLabel
                refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
                // 执行下拉刷新逻辑任务
                new GetDataTask().execute();
            }
        });

        actualListView = houseInfoList.getRefreshableView();

        // 从数据库中获取相关数据，并显示出来
        dataLists = DatabaseQueryHelper.getInstance(getActivity().getApplicationContext()).getHouseInfoFromDb();

        mAdapter = new HousInfoAdapter(dataLists, getActivity());
        actualListView.setAdapter(mAdapter);
        actualListView.addHeaderView(emptyHeader);

        actualListView.setSelection(curPos);

        ObjectAnimator animator = ObjectAnimator.ofFloat(actualListView, "alpha", 0.0f, 1f);
        animator.setDuration(1000);
        animator.start();

        actualListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                try {
                    houseHeadText.setText(mAdapter.getItem(firstVisibleItem).getHouseId());
                    curPos = firstVisibleItem;
                } catch (Exception e) {}

            }
        });

        // 设置滑动
        actualListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mCurrPosY = event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mFirstPosY = event.getRawY();
                        mLastY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mCurrPosY - mFirstPosY > EXTRA_NUM * mTouchSlop) {
                           // DOWN
                            if (curDirection == UP) {
                                curDirection = SCROLLING;
                                inAnimator = ObjectAnimator.ofFloat(headView, "translationY",
                                        headView.getTranslationY(), headView.getTranslationY() + taskRemainView.getHeight());
                                inAnimator.setDuration(500);
                                inAnimator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        curDirection = DOWN;
                                        super.onAnimationEnd(animation);
                                    }
                                });
                                inAnimator.start();
                            }
                        } else if (mFirstPosY - mCurrPosY > taskRemainView.getHeight()) {
                            // UP
                            if (curDirection == DOWN) {
                                curDirection = SCROLLING;
                                outAnimator = ObjectAnimator.ofFloat(headView, "translationY",
                                        headView.getTranslationY(), headView.getTranslationY() - taskRemainView.getHeight());
                                outAnimator.setDuration(500);
                                outAnimator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        curDirection = UP;
                                        super.onAnimationEnd(animation);
                                    }
                                });
                                outAnimator.start();
                            }
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }

                mLastY = mCurrPosY;
                return false;
            }
        });

        actualListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if ( (id >= 0) && (id < dataLists.size())) {
                Intent intent = new Intent(getActivity(), BedInfoActivity.class);
                intent.putExtra(BedInfoActivity.BEDID_INFOS, getBedIds(dataLists.get((int)id)));
                startActivity(intent);
            }
            }
        });
    }

    // 组装发送给BedInfo的字符串
    private String getBedIds(HouseInfoItem item) {
        String data = "";
        if (item != null) {
            data += item.getHouseId() + " ";
            List<String> bed_list = item.getBed_ids();

            for (String bed_id : bed_list)
                data += bed_id + " ";
        }
        return data;
    }

    private <T> T $(View view, int resId) {
        return (T)view.findViewById(resId);
    }


    // 异步获取数据模块
    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            // Simulates a background job.
            try {
                Thread.sleep(1000);
                dataLists = DatabaseQueryHelper.getInstance(getActivity().getApplicationContext()).getHouseInfoFromDb();
            } catch (InterruptedException e) {
            }
            return null;
        }

        // 下拉刷新之后的响应
        @Override
        protected void onPostExecute(String[] result) {
            mAdapter.notifyDataSetChanged();

            houseInfoList.onRefreshComplete();

            super.onPostExecute(result);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }

}
