package com.echo.staywhere;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class PriceChartFormatter implements IAxisValueFormatter {

    private String[] mValues;

    public PriceChartFormatter(String[] values) {
        //2017-09 --> 09/17
        String[] formattedValues = new String[values.length];
        for(int i = 0; i < values.length; i ++){
            String value = values[i];
            String[] valueRow = value.substring(2).split("-");
            String formmatedValue = valueRow[1] + "/" + valueRow[0];
            formattedValues[i] = formmatedValue;
        }
        this.mValues = formattedValues;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        return mValues[(int) value];
    }

}