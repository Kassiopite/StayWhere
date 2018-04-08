package com.echo.staywhere;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CompareFragment extends Fragment {

    private static final String ARG_RESPONSE = "response";
    private String response;
    private Activity activity;
    private LineChart chart;

    public CompareFragment() {
        // Required empty public constructor
    }

    public static CompareFragment newInstance(String response) {
        CompareFragment fragment = new CompareFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RESPONSE, response);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            response = getArguments().getString(ARG_RESPONSE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compare, container, false);

        chart = view.findViewById(R.id.compare_chart);

        String[] labels = {"", ""};

        try {
            if (response != null) {

                JSONArray results = new JSONArray(response);
                ArrayList<PriceChartData[]> dataSet = new ArrayList<>();
                for (int i = 0; i < 2; i++) {

                    JSONObject result = results.getJSONObject(i);
                    String roomType = result.getString("roomType");
                    String postal = result.getString("postal");
                    labels[i] = roomType + " @ " + postal;

                    JSONArray dataRow = result.getJSONArray("data");
                    if (dataRow != null) {
                        PriceChartData[] data = new PriceChartData[6];
                        for (int j = 0; j < 6; j++) {

                            JSONObject monthData = dataRow.getJSONObject(j);
                            String month = monthData.getString("month");
                            float amount = monthData.getInt("avgAmount");
                            PriceChartData dataItem = new PriceChartData(amount, month);
                            data[j] = dataItem;
                        }
                        dataSet.add(data);
                    }
                }
                setupChart(dataSet, labels);
            } else {
                setupChart(null, labels);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ImageButton btnDismiss = view.findViewById(R.id.btn_dismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
                ft.remove(CompareFragment.this).commit();
            }
        });
        return view;
    }

    private void setupChart(ArrayList<PriceChartData[]> dataSet, String[] labels) {

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);

        int[] colors = {Color.BLUE, Color.GREEN};

        if (dataSet != null && dataSet.size() == 2) {

            XAxis xAxis = chart.getXAxis();

            LineData lineData = new LineData();

            for (int i = 0; i < 2; i++) {
                PriceChartData[] data = dataSet.get(i);
                String[] dates = new String[data.length];

                List<Entry> entries = new ArrayList<Entry>();
                for (int j = 0; j < data.length; j++) {
                    entries.add(new Entry(j, data[j].getPrice()));
                    dates[j] = data[j].getDate();
                }

                LineDataSet dataItem = new LineDataSet(entries, labels[i]);
                dataItem.setColor(colors[i]);
                dataItem.setValueTextColor(Color.BLACK);
                lineData.addDataSet(dataItem);
                xAxis.setValueFormatter(new PriceChartFormatter(dates));
            }

            lineData.setValueTextSize(18);
            chart.setData(lineData);

            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(14);
            xAxis.setGranularity(1f);
            float max = xAxis.getAxisMaximum();
            float min = xAxis.getAxisMinimum();
            xAxis.setAxisMaximum(max + 0.2f);
            xAxis.setAxisMinimum(min - 0.2f);

            YAxis yAxisLeft = chart.getAxisLeft();
            YAxis yAxisRight = chart.getAxisRight();
            yAxisLeft.setEnabled(false);
            yAxisRight.setEnabled(false);
            yAxisLeft.setDrawGridLines(false);


        } else {
            chart.setNoDataText("No data available");
        }
        chart.invalidate();
    }

}
