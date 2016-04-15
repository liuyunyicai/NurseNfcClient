package hust.nursenfcclient.helps;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import hust.nursenfcclient.R;
import hust.nursenfcclient.patient.BedInfoActivity;
import hust.nursenfcclient.patient.TemperInfoItem;

/**
 * Created by admin on 2015/12/3.
 */
public class ChartHelper {

    // 设置显示的样式
    public static void setupChart(LineChart chart, LineData data) {

        chart.setDescription("");// 数据描述
        // 如果没有数据的时候，会显示这个，类似listview的emtpyview
        chart.setNoDataTextDescription("暂时没有温度数据记录");
        chart.setDrawGridBackground(false); // 是否显示表格颜色

        chart.setTouchEnabled(true); // 设置是否可以触摸
        chart.setDragEnabled(true);// 是否可以拖拽
        chart.setScaleEnabled(true);// 是否可以缩放
        chart.setPinchZoom(false);//
        chart.setBackgroundColor(Color.WHITE);// 设置背景

        // 自定义字体
//        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // ====== 设置标准线  ======
        LimitLine ll1 = new LimitLine(BedInfoActivity.UPPER_TEMPER, "高烧危险");
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(1.0f, 1.0f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
//        ll1.setTypeface(tf);

        LimitLine ll2 = new LimitLine(BedInfoActivity.LOWER_TEMPER, "低烧危险");
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(1.0f, 1.0f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
//        ll2.setTypeface(tf);

        // ====== 设置Y轴显示 ======
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll1);   // 添加标准线
        leftAxis.addLimitLine(ll2);
        leftAxis.setAxisMaxValue(BedInfoActivity.MAX_TEMPER);
        leftAxis.setAxisMinValue(BedInfoActivity.MIN_TEMPER);
        leftAxis.setStartAtZero(false);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(1.0f, 1.0f, 0f);
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        chart.getAxisRight().setEnabled(false);


        // ======设置数据=======
        chart.setData(data);

        chart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//        mChart.invalidate();
        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
    }

    // 生成数据
    public static LineData getData(List<TemperInfoItem> lists, float max, float min, Context context) {
        if (lists == null) return null;

        int count = lists.size();
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            // x轴显示的数据，这里默认使用数字下标显示
            xVals.add(lists.get(i).getLast_time().substring(11, 16));
        }

        // y轴的数据
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        for (int i = 0; i < count; i++) {
            float val = lists.get(i).getTemper_num();
            yVals.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        // y轴的数据集合
        LineDataSet set1 = new LineDataSet(yVals, "病人一天温度表");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        set1.setLineWidth(1.75f); // 线宽
        set1.setCircleSize(3f);// 显示的圆形大小
        set1.setColor(context.getResources().getColor(R.color.id_blue));// 显示颜色
        set1.setCircleColor(context.getResources().getColor(R.color.colorPrimary));// 圆形的颜色
        set1.setHighLightColor(context.getResources().getColor(R.color.id_blue)); // 高亮的线的颜色

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        return data;
    }
}
