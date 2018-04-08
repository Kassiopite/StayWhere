package com.echo.staywhere;


public class Constant {

    //shared preference
    public static final String SP = "SHARED_PREFERENCE";
    public static final String SP_COUNTER = "COUNTER";
    public static final String SP_BOOKMARK = "BOOKMARK";

    //command types
    public static final int GET_LOCATION = 1;
    public static final int GET_PRICE_LIST = 2;
    public static final int GET_PRICE_LIST_SECOND = 3;
    public static final int GET_COMPARE = 4;

    //google maps/place api key
    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyD0E1D5xoXRELmpoUZznk1jh6hZUgW0X24";

    //api url
    private static final String MAPS_URL_PREFIX = "https://maps.googleapis.com/maps/api/";
    public static final String MAPS_GET_LOCATION = MAPS_URL_PREFIX + "geocode/json?";
    public static final String MAPS_NEARBY = MAPS_URL_PREFIX + "place/nearbysearch/json?";

    public static final String GOV_URL_PREFIX = "http://192.168.1.99:3000/dataGovSg/";
    public static final String GOV_SEARCH = GOV_URL_PREFIX + "search/";
    public static final String GOV_COMPARE = GOV_URL_PREFIX + "compare";

    //prefix
    public static final String PREFIX_ADDR = "address=";
    public static final String PREFIX_API_KEY = "&key=";
    public static final String PREFIX_LOCATION = "location=";//1.31695,103.7685
    public static final String RADIUS = "&radius=500";
    public static final String PREFIX_TYPE = "&type=";
    public static final String TRANSIT_TYPE = "transit_station";
    public static final String HOSPITAL_TYPE = "hospital";
    public static final String MALL_TYPE = "store";

}
