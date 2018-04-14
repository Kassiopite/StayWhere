package com.echo.staywhere;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceActivity extends AppCompatActivity implements OnMapReadyCallback, HttpRequest.AsyncResponse, AdapterView.OnItemSelectedListener {

    private double lat, lng;
    private boolean main;
    private int commandType, roomType;
    private TextView tvAddr, tvAddrDetail;
    private GoogleMap map;
    private SharedPreferences sp;
    private Spinner spinner;
    private ImageButton btnBookmark;
    private LineChart chart;
    private String searchReponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        sp = getSharedPreferences(Constant.SP, Context.MODE_PRIVATE);

        Intent i = getIntent();
        lat = i.getDoubleExtra("lat", 0);
        lng = i.getDoubleExtra("lng", 0);
        roomType = i.getIntExtra("type", -1);
        main = i.getBooleanExtra("main", false);

        //map section
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //search section
        View vSearch = findViewById(R.id.search_section);

        final EditText searchBox = findViewById(R.id.search_box);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String input = searchBox.getText().toString().trim();
                String getLocationURL = Constant.MAPS_GET_LOCATION + Constant.PREFIX_ADDR + input
                        + Constant.PREFIX_API_KEY + Constant.GOOGLE_MAPS_API_KEY;
                commandType = Constant.GET_LOCATION;
                new HttpRequest(PlaceActivity.this, PlaceActivity.this).execute(getLocationURL);
            }
        });

        final ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //place section
        tvAddr = findViewById(R.id.addr);
        tvAddrDetail = findViewById(R.id.addr_detail);
        setAddress(lat, lng, i.getStringExtra("addr"));

        spinner = findViewById(R.id.spinner_room_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.room_types_array, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        btnBookmark = findViewById(R.id.btn_bookmark);
        btnBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnBookmark.getTag().equals(0)) {
                    String postalCode = tvAddr.getText().toString().trim().substring(10);
                    int roomType = spinner.getSelectedItemPosition();

                    int counter = sp.getInt(Constant.SP_COUNTER, 0);
                    String bookmarks = sp.getString(Constant.SP_BOOKMARK, "");

                    String removeBookmark = postalCode + roomType;
                    bookmarks = bookmarks.replaceAll(removeBookmark, "");
                    bookmarks = bookmarks.replaceAll("\\|\\|", "|");
                    String updateBookmarks = bookmarks;
                    if (bookmarks.startsWith("|"))
                        updateBookmarks = bookmarks.replaceFirst("\\|", "");
                    if (bookmarks.endsWith("|"))
                        updateBookmarks = bookmarks.substring(0, bookmarks.length() - 1);

                    sp.edit().putInt(Constant.SP_COUNTER, --counter).apply();
                    sp.edit().putString(Constant.SP_BOOKMARK, updateBookmarks).apply();

                    btnBookmark.setImageResource(R.mipmap.icon_heart_dim);
                    btnBookmark.setTag(1);
                } else {
                    String postalCode = tvAddr.getText().toString().trim().substring(10);
                    int roomType = spinner.getSelectedItemPosition();

                    int counter = sp.getInt(Constant.SP_COUNTER, 0);
                    String bookmarks = sp.getString(Constant.SP_BOOKMARK, "");

                    StringBuilder updateBookmark = new StringBuilder();
                    updateBookmark.append(bookmarks);
                    if (!bookmarks.equals("")) updateBookmark.append("|");
                    updateBookmark.append(postalCode).append(roomType);

                    sp.edit().putInt(Constant.SP_COUNTER, ++counter).apply();
                    sp.edit().putString(Constant.SP_BOOKMARK, updateBookmark.toString()).apply();

                    btnBookmark.setImageResource(R.mipmap.icon_heart_active);
                    btnBookmark.setTag(0);
                }
            }
        });

        //already bookmarked
        checkBookmark();

        ImageButton btnNearby = findViewById(R.id.btn_nearby);
        btnNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaceActivity.this, AmenitiesActivity.class);
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
                startActivity(intent);
            }
        });

        ImageButton btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = tvAddr.getText().toString().trim();
                String addrDetail = tvAddrDetail.getText().toString().trim();
                Intent intent = new Intent(PlaceActivity.this, PosterActivity.class);
                intent.putExtra("addr", address);
                intent.putExtra("addr_detail", addrDetail);
                startActivity(intent);
            }
        });

        // get price data
        String postalCode = tvAddr.getText().toString().trim().substring(10);
        commandType = Constant.GET_PRICE_LIST;
        String getPriceList = Constant.GOV_SEARCH + postalCode;
        new HttpRequest(PlaceActivity.this, PlaceActivity.this).execute(getPriceList);

        chart = findViewById(R.id.chart);

        //cater for different source page
        if (!main) {
            vSearch.setVisibility(View.GONE);
            btnBookmark.setImageResource(R.mipmap.icon_heart_active);
        }
    }

    private void setupChart(PriceChartData[] data) {

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        if (data != null && data.length > 0) {
            String[] dates = new String[data.length];

            List<Entry> entries = new ArrayList<Entry>();
            for (int j = 0; j < 6; j++) {
                entries.add(new Entry(j, data[j].getPrice()));
                dates[j] = data[j].getDate();
            }

            LineDataSet dataSet1 = new LineDataSet(entries, ""); // add entries to dataset
            dataSet1.setColor(Color.BLUE);
            dataSet1.setValueTextColor(Color.BLACK);

            LineData lineData = new LineData();
            lineData.addDataSet(dataSet1);
            lineData.setValueTextSize(16);
            chart.setData(lineData);

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(14);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new PriceChartFormatter(dates));

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

    private void checkBookmark() {
        String bookmarks = sp.getString(Constant.SP_BOOKMARK, "");
        String postalCode = tvAddr.getText().toString().trim().substring(10);
        int roomType = spinner.getSelectedItemPosition();
        StringBuilder currentPlace = new StringBuilder();
        currentPlace.append(postalCode).append(roomType);
        if (bookmarks.contains(currentPlace.toString().trim())) {
            btnBookmark.setImageResource(R.mipmap.icon_heart_active);
            btnBookmark.setTag(0);
        } else {
            btnBookmark.setImageResource(R.mipmap.icon_heart_dim);
            btnBookmark.setTag(1);
        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        updateMap();
    }

    @Override
    public void onPostRequest(String output) {
        switch (commandType) {
            case Constant.GET_LOCATION:
                try {
                    JSONObject reader = new JSONObject(output);
                    String status = reader.getString("status");
                    if (status.equals("OK")) {
                        JSONArray results = reader.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject result = results.getJSONObject(i);
                            String formattedAddress = result.getString("formatted_address");
                            if (formattedAddress.contains("Singapore")) {
                                JSONObject geometry = result.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                lat = location.getDouble("lat");
                                lng = location.getDouble("lng");
                                setAddress(lat, lng, formattedAddress);
                                updateMap();

                                // get price data
                                commandType = Constant.GET_PRICE_LIST_SECOND;
                                String postalCode = tvAddr.getText().toString().trim().substring(10);
                                String getPriceList = Constant.GOV_SEARCH + postalCode;
                                new HttpRequest(PlaceActivity.this, PlaceActivity.this).execute(getPriceList);
                                break;
                            } else
                                Toast.makeText(this, R.string.invalid_postal_code, Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Toast.makeText(this, R.string.invalid_postal_code, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.invalid_postal_code, Toast.LENGTH_SHORT).show();
                }
                break;
            case Constant.GET_PRICE_LIST_SECOND:
                int pos = spinner.getSelectedItemPosition();
                int newPos;
                if (pos == 4) newPos = 3;
                else newPos = pos + 1;
                spinner.setSelection(newPos);
                searchReponse = output;
                break;
            case Constant.GET_PRICE_LIST:
                if (roomType != -1 && !main) {
                    spinner.setSelection(roomType);
                } else {
                    spinner.setSelection(1);
                }
                searchReponse = output;
                break;
        }

    }

    private void updateMap() {
        LatLng position = new LatLng(lat, lng);
        map.clear();
        MarkerOptions marker = new MarkerOptions().position(position).title("");
        map.addMarker(marker);
        map.moveCamera(CameraUpdateFactory.newLatLng(position));
        map.setMinZoomPreference(12.0f);
    }

    private void setAddress(double latitude, double longitude, String defaultAddr) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String addr = address.getCountryName() + " " + address.getPostalCode();
                tvAddr.setText(addr);
                String addrDetail = address.getSubThoroughfare() + " " + address.getThoroughfare();
                tvAddrDetail.setText(addrDetail);
            }
        } catch (IOException e) {
            tvAddr.setText(defaultAddr);
            tvAddrDetail.setText("");
            e.printStackTrace();
        }

    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        checkBookmark();

        String[] roomTypes = getResources().getStringArray(R.array.room_types_array);
        String roomType = roomTypes[pos].toUpperCase();
        try {
            if (searchReponse != null) {
                JSONArray results = new JSONArray(searchReponse);
                PriceChartData[] data = new PriceChartData[results.length()];
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    String month = result.getString("month");
                    JSONObject rowData = result.getJSONObject("data");
                    JSONObject selectedData = rowData.getJSONObject(roomType);
                    float amount = selectedData.getInt("avgAmount");
                    PriceChartData dataRow = new PriceChartData(amount, month);
                    data[i] = dataRow;
                }
                setupChart(data);
            } else {
                setupChart(null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

}
