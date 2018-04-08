package com.echo.staywhere;

public class PriceChartData {

    private float price;
    private String date;

    public PriceChartData(float price, String date) {
        this.price = price;
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

}
