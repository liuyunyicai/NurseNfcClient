package com.handmark.pulltorefresh.library;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ListView;

/**
 * Created by admin on 2015/11/18.
 */

/** 定义弹性滑动的ListView  **/
public class MyListView extends ListView {
    private int maxOverScrollDistanceY = 80;

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density; // 获取屏幕密度
        maxOverScrollDistanceY = (int) (maxOverScrollDistanceY * density);

    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
                                   int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        // 将maxOverScrollY滑动距离替换为maxOverScrollDistanceY即可，因为其默认值为0
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
                maxOverScrollX, maxOverScrollDistanceY, isTouchEvent);
    }

    public void setMaxOverScrollDistanceY(int maxOverScrollDistanceY) {
        this.maxOverScrollDistanceY = maxOverScrollDistanceY;
    }
}
