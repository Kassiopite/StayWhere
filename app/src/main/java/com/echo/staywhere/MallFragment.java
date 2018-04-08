package com.echo.staywhere;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MallFragment extends Fragment implements HttpRequest.AsyncResponse {

    Context context;
    ArrayList<String> malls;

    private static final String ARG_LAT = "lat";
    private static final String ARG_LNG = "lng";

    private double lat;
    private double lng;

    private ListView listMall;

    public MallFragment() {
        // Required empty public constructor
    }

    public static MallFragment newInstance(double lat, double lng) {
        MallFragment fragment = new MallFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lat = getArguments().getDouble(ARG_LAT);
            lng = getArguments().getDouble(ARG_LNG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mall, container, false);
        listMall = view.findViewById(R.id.list_mall);

        malls = new ArrayList<>();
        String location = lat + "," + lng;
        String getAmenitiesURL = Constant.MAPS_NEARBY + Constant.PREFIX_LOCATION + location
                + Constant.RADIUS + Constant.PREFIX_TYPE + Constant.MALL_TYPE
                + Constant.PREFIX_API_KEY + Constant.GOOGLE_MAPS_API_KEY;
        new HttpRequest(this, context).execute(getAmenitiesURL);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onPostRequest(String output) {
        try {
            JSONObject reader = new JSONObject(output);
            String status = reader.getString("status");
            if (status.equals("OK")) {
                JSONArray results = reader.getJSONArray("results");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    String name = result.getString("name");
                    malls.add(name);
                }
            } else
                Toast.makeText(context, R.string.invalid_response, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.invalid_response, Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter adapterMall = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, malls);
        listMall.setAdapter(adapterMall);
    }


}
