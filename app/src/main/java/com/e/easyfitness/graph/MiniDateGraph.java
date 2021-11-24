package com.e.easyfitness.graph;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.e.easyfitness.R;
import com.e.easyfitness.utils.DateConverter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

public class MiniDateGraph {

    private LineChart mChart = null;
    private String mChartName = null;
    private Context mContext = null;

    public MiniDateGraph(Context context, LineChart chart, String name) {
        mChart = chart;
        mChartName = name;
        mChart.getDescription().setEnabled(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setHorizontalScrollBarEnabled(false);
        mChart.setVerticalScrollBarEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(false);
        mChart.setDrawBorders(false);
        mChart.setViewPortOffsets(6f, 6f, 6f, 6f);
        mChart.animateY(1000, Easing.EaseInOutBack); // animate horizontal 3000 milliseconds
        mChart.setClickable(false);

        mChart.getAxisRight().setDrawLabels(false);
        mChart.getAxisLeft().setDrawLabels(false);
        mChart.getLegend().setEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDescription(null);
        mChart.setTouchEnabled(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setNoDataText(context.getString(R.string.no_chart_data_available));

        mContext = context;
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ColorTemplate.getHoloBlue());
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1); // 1 jour

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setEnabled(false);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(false);

        mChart.getAxisRight().setEnabled(false);
    }

    public void draw(ArrayList<Entry> entries) {
        mChart.clear();
        if (entries.isEmpty()) {
            return;
        }

        Collections.sort(entries, new EntryXComparator());

        //Log.d("DEBUG", arrayToString(entries));

        LineDataSet set1 = new LineDataSet(entries, mChartName);
        set1.setLineWidth(3f);
        set1.setCircleRadius(0f);
        set1.setDrawFilled(true);
        if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.fade_blue);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(ColorTemplate.getHoloBlue());
        }
        set1.setFillAlpha(100);
        set1.setColor(mContext.getResources().getColor(R.color.toolbar_background));
        set1.setCircleColor(mContext.getResources().getColor(R.color.toolbar_background));

        // Create a data object with the datasets
        LineData data = new LineData(set1);
        data.setDrawValues(false);

        /*data.setValueFormatter(new IValueFormatter() {
            private DecimalFormat mFormat = new DecimalFormat("#.##");
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value);
            }
        });*/

        // Set data
        mChart.setData(data);

        mChart.invalidate();
        //mChart.animateY(500, Easing.EasingOption.EaseInBack);    //refresh graph

    }

    private String arrayToString(ArrayList<Entry> entries) {
        StringBuilder output = new StringBuilder();
        String delimiter = "\n"; // Can be new line \n tab \t etc...
        for (int i = 0; i < entries.size(); i++) {
            output.append(entries.get(i).getY()).append(" / ").append(entries.get(i).getX()).append(delimiter);
        }

        return output.toString();
    }

    public LineChart getChart() {
        return mChart;
    }

    public void setZoom(zoomType z) {
        switch (z) {
            case ZOOM_ALL:
                mChart.fitScreen();
                break;
            case ZOOM_WEEK:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 7); // allow 20 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() + (1 - 7)); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_MONTH:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 30); // allow 30 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() + (float) (1 - 30)); // set the left edge of the chart to x-index 10
                }
                break;
            case ZOOM_YEAR:
                mChart.fitScreen();
                if (mChart.getData() != null) {
                    mChart.setVisibleXRangeMaximum((float) 365); // allow 365 values to be displayed at once on the x-axis, not more
                    mChart.moveViewToX(mChart.getData().getXMax() + (float) (1 - 365)); // set the left edge of the chart to x-index 10
                }
                break;
        }

        // refresh
        mChart.invalidate();
    }

    public enum zoomType {ZOOM_ALL, ZOOM_YEAR, ZOOM_MONTH, ZOOM_WEEK}
}