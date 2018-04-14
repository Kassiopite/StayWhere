package com.echo.staywhere;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

public class TransportFragment extends Fragment implements HttpRequest.AsyncResponse {

    Context context;
    ArrayList<Amenity> transports;

    private static final String ARG_LAT = "lat";
    private static final String ARG_LNG = "lng";

    private double lat;
    private double lng;

    private ListView listMRT;
    private ListView listBus;


    public TransportFragment() {
        // Required empty public constructor
    }

    public static TransportFragment newInstance(Double lat, Double lng) {
        TransportFragment fragment = new TransportFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LNG, lng);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
        View view = inflater.inflate(R.layout.fragment_transport, container, false);
        listMRT = view.findViewById(R.id.list_mrt);
        listBus = view.findViewById(R.id.list_bus);

        transports = new ArrayList<>();
        String location = lat + "," + lng;
        String getAmenitiesURL = Constant.MAPS_NEARBY + Constant.PREFIX_LOCATION + location
                + Constant.RADIUS + Constant.PREFIX_TYPE + Constant.TRANSIT_TYPE
                + Constant.PREFIX_API_KEY + Constant.GOOGLE_MAPS_API_KEY;
        new HttpRequest(this, context).execute(getAmenitiesURL);

        return view;
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
                    String types = result.getJSONArray("types").toString();
                    String name = result.getString("name");
                    if (types.contains("subway_station")) {
                        transports.add(new Amenity("subway_station", name));
                    } else if (types.contains("bus_station")) {
                        transports.add(new Amenity("bus_station", name));
                    }
                }
            } else if (!status.equals("ZERO_RESULTS"))
                Toast.makeText(context, R.string.invalid_response, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.invalid_response, Toast.LENGTH_SHORT).show();
        }

        ArrayList<String> mrts = new ArrayList<>();
        ArrayList<String> buses = new ArrayList<>();
        for (Amenity amenity : transports) {
            String type = amenity.getType();
            if (type.equals("subway_station")) {
                mrts.add(amenity.getName());
            } else if (type.equals("bus_station")) {
                buses.add(amenity.getName());
            }
        }

        ArrayAdapter adapterMRT = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, mrts);
        ArrayAdapter adapterBus = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, buses);

        listMRT.setAdapter(adapterMRT);
        listBus.setAdapter(adapterBus);
    }

}
