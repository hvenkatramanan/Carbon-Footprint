package com.example.hariharanvenkatramanan.carbonf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by hariharanvenkatramanan on 3/18/17.
 */


public class NavigationActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = "Navigation Activity";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBX_ZPeIFsNEG4s4KXlN2NWw86uKfLlT68";

    private double src_lat;
    private double src_lon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.navigation_main_layout);
        LocationUtils.startLocationUpdates(this);
        final Context c = this;

        final LinearLayout results = (LinearLayout)findViewById(R.id.linearLayout_results);
        final Geocoder geocoder = new Geocoder(getApplicationContext());
       // final EditText from_loc_edittext = (EditText)findViewById(R.id.autoCompleteTextView_from);
        //final EditText to_loc_edittext = (EditText)findViewById(R.id.autoCompleteTextView_to);
        final TextView car_time_tv = (TextView)findViewById(R.id.car_timing);
        final TextView bus_time_tv = (TextView)findViewById(R.id.bus_timing);
        final TextView recommendation_tv = (TextView)findViewById(R.id.textView_recommendation);


        final String car_time = "";
        final String bus_time = "";

        ImageButton button_loc = (ImageButton) findViewById(R.id.getLocationButton);
        ImageButton button_send = (ImageButton) findViewById(R.id.sendButton);

        results.setVisibility(View.INVISIBLE);


        final AutoCompleteTextView autoCompView_from = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_from);
        autoCompView_from.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView_from.setOnItemClickListener(this);

        final AutoCompleteTextView autoCompView_to = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_to);
        autoCompView_to.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView_to.setOnItemClickListener(this);

        button_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                src_lat = LocationUtils.getLat();
                src_lon = LocationUtils.getLon();
                try {
                    List<Address> address = geocoder.getFromLocation(src_lat,src_lon,1);
                    Address a = address.get(0);
                    autoCompView_from.setText(a.getAddressLine(0).toString()+a.getAddressLine(1).toString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = getCurrentFocus();
                if (view2 != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                if(autoCompView_to.getText().toString().isEmpty()){
                    String s = "Enter Destination";
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
                else{
                    results.setVisibility(View.VISIBLE);

                    car_time_tv.setText(car_time+"Mins");
                    bus_time_tv.setText(bus_time+"Mins");
                    recommendation_tv.setText("Please take the bus to reduce Carbon Footprint. The World thanks you!");


                }
            }
        });

    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:us");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
           } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;
        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }
        @Override
        public String getItem(int index) {
            return resultList.get(index).toString();
        }
        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());
                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationUtils.startLocationUpdates(this);

    }
}
