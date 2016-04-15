package hust.nursenfcclient.house;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.twotoasters.jazzylistview.JazzyListView;

/**
 * Created by admin on 2015/11/19.
 */
public class AnimationListView extends JazzyListView {
    private int maxOverScrollDistanceY = 180;

    public AnimationListView(Context context) {
        super(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density; // 获取屏幕密度
        maxOverScrollDistanceY = (int) (maxOverScrollDistanceY * density);
    }

    public AnimationListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
