package com.echo.staywhere;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements HttpRequest.AsyncResponse {

    private int commandType;
    private ListView listBookmark;
    private View sectionHint;
    private SharedPreferences sp;
    private boolean main = true;
    private ArrayList<String> bookmarkList = new ArrayList<>();
    private int roomType = -1;
    private ArrayList<String> selectedList = new ArrayList<>();
    private ImageButton btnCompare;
    private TextView tvCompareHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText searchBox = findViewById(R.id.search_box);
        final ImageButton btnSearch = findViewById(R.id.btn_search);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main = true;
                commandType = Constant.GET_LOCATION;
                String input = searchBox.getText().toString().trim();
                String getLocationURL = Constant.MAPS_GET_LOCATION + Constant.PREFIX_ADDR + input
                        + Constant.PREFIX_API_KEY + Constant.GOOGLE_MAPS_API_KEY;
                new HttpRequest(MainActivity.this, MainActivity.this).execute(getLocationURL);
            }
        });

        sp = getSharedPreferences(Constant.SP, Context.MODE_PRIVATE);

        listBookmark = findViewById(R.id.list_bookmark);
        sectionHint = findViewById(R.id.bookmark_hint);
        btnCompare = findViewById(R.id.btn_compare);
        tvCompareHint = findViewById(R.id.compare_hint);

        updateBookmarkList();
        listBookmark.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                main = false;
                String formattedBookmark = bookmarkList.get(position);
                String[] datas = formattedBookmark.split("@");
                String postalCode = datas[1].trim();
                String type = datas[0].trim();
                String[] roomTypes = getResources().getStringArray(R.array.room_types_array);
                for (int j = 0; j < roomTypes.length; j++) {
                    if (type.equals(roomTypes[j])) {
                        roomType = j;
                        break;
                    }
                }
                commandType = Constant.GET_LOCATION;
                String getLocationURL = Constant.MAPS_GET_LOCATION + Constant.PREFIX_ADDR + postalCode
                        + Constant.PREFIX_API_KEY + Constant.GOOGLE_MAPS_API_KEY;
                new HttpRequest(MainActivity.this, MainActivity.this).execute(getLocationURL);
            }
        });

        listBookmark.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.getTag() != null && view.getTag().equals(true)) {
                    view.setTag(false);
                    view.setBackgroundColor(Color.WHITE);
                    String formattedBookmark = bookmarkList.get(position);
                    String[] datas = formattedBookmark.split("@");
                    String selectedPostalCode = datas[1].trim();
                    String type = datas[0].trim().toUpperCase();
                    selectedList.remove(selectedPostalCode + "|" + type);
                } else {
                    int checkCount = selectedList.size();
                    if (checkCount < 2) {
                        view.setTag(true);
                        view.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));

                        String formattedBookmark = bookmarkList.get(position);
                        String[] datas = formattedBookmark.split("@");
                        String selectedPostalCode = datas[1].trim();
                        String type = datas[0].trim().toUpperCase();
                        selectedList.add(selectedPostalCode + "|" + type);
                    } else
                        Toast.makeText(MainActivity.this, R.string.maxi_seclection, Toast.LENGTH_SHORT).show();
                }

                if (selectedList.size() == 2) {
                    tvCompareHint.setVisibility(View.GONE);
                    btnCompare.setVisibility(View.VISIBLE);
                } else {
                    tvCompareHint.setVisibility(View.VISIBLE);
                    btnCompare.setVisibility(View.GONE);
                }
                return false;
            }
        });

        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commandType = Constant.GET_COMPARE;

                String[] select1 = selectedList.get(0).split("\\|");
                String[] select2 = selectedList.get(1).split("\\|");
                String[] compareParams = new String[5];
                compareParams[0] = Constant.GOV_COMPARE;
                compareParams[1] = "postal1|" + select1[0];
                compareParams[2] = "room1|" + select1[1];
                compareParams[3] = "postal2|" + select2[0];
                compareParams[4] = "room2|" + select2[1];

                new HttpRequest(MainActivity.this, MainActivity.this).execute(compareParams);
                updateBookmarkList();
            }
        });

    }

    private void updateBookmarkList() {

        selectedList.clear();
        btnCompare.setVisibility(View.GONE);
        int counter = sp.getInt(Constant.SP_COUNTER, 0);
        if (counter == 0) {
            sectionHint.setVisibility(View.VISIBLE);
            listBookmark.setVisibility(View.GONE);
            tvCompareHint.setVisibility(View.GONE);
        } else {
            sectionHint.setVisibility(View.GONE);
            listBookmark.setVisibility(View.VISIBLE);
            tvCompareHint.setVisibility(View.VISIBLE);
        }

        bookmarkList = new ArrayList<>();
        String bookmarks = sp.getString(Constant.SP_BOOKMARK, "");
        if (!bookmarks.equals("")) {
            String[] bookmarkArray = bookmarks.split("\\|");
            for (int i = 0; i < bookmarkArray.length; i++) {
                String item = bookmarkArray[i];
                String[] roomTypes = getResources().getStringArray(R.array.room_types_array);
                String formattedBookmark = roomTypes[Integer.parseInt(item.substring(6))] +
                        " @ " + item.substring(0, 6);
                bookmarkList.add(formattedBookmark);
            }
        }
        ArrayAdapter adapterBookmark = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookmarkList);
        listBookmark.setAdapter(adapterBookmark);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateBookmarkList();
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
                                Double lat = location.getDouble("lat");
                                Double lng = location.getDouble("lng");
                                Intent intent = new Intent(this, PlaceActivity.class);
                                intent.putExtra("addr", formattedAddress);
                                intent.putExtra("lat", lat);
                                intent.putExtra("lng", lng);
                                intent.putExtra("type", roomType);
                                intent.putExtra("main", main);
                                startActivity(intent);
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
            case Constant.GET_COMPARE:
                Fragment compareFragment = CompareFragment.newInstance(output);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.add(R.id.frame_container, compareFragment).commit();
                break;
        }

    }

}
