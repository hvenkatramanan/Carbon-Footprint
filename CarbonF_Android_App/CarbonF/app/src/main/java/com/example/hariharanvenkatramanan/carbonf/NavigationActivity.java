package com.example.hariharanvenkatramanan.carbonf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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

import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static LatLng src;
    private static LatLng dstn = null;

    private int car_time ;
    private int bus_time;
    private String distance = "";
    private String duration = "";

    private  TextView car_time_tv;
    private  TextView bus_time_tv;
    private TextView recommendation_tv;
    private boolean carDone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.navigation_main_layout);
        LocationUtils.startLocationUpdates(this);
        final Context c = this;

        car_time_tv = (TextView)findViewById(R.id.car_timing);
        bus_time_tv = (TextView)findViewById(R.id.bus_timing);
        final LinearLayout results = (LinearLayout)findViewById(R.id.linearLayout_results);
        final Geocoder geocoder = new Geocoder(getApplicationContext());
       // final EditText from_loc_edittext = (EditText)findViewById(R.id.autoCompleteTextView_from);
        //final EditText to_loc_edittext = (EditText)findViewById(R.id.autoCompleteTextView_to);

        recommendation_tv = (TextView)findViewById(R.id.textView_recommendation);




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
                src = new LatLng(src_lat,src_lon);

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
                    dstn = new LatLng(0,0);
                    dstn = getLocationFromAddress(c, autoCompView_to.getText().toString());
                    src_lat = LocationUtils.getLat();
                    src_lon = LocationUtils.getLon();
                    src = new LatLng(src_lat,src_lon);

                    String url = getDirectionsUrl(src, dstn, "driving");

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);

                    DownloadTask downloadTask1 = new DownloadTask();
                    String url1 = getDirectionsUrl(src, dstn, "transit");
                    downloadTask1.execute(url1);
                }
            }
        });

    }


    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }



    private String getDirectionsUrl(LatLng origin,LatLng dest, String mode){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        String key = "key="+API_KEY;

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&mode="+mode+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception downloading", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        public DownloadTask(){
            super();
        }

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
           // PolylineOptions lineOptions = null;
            //MarkerOptions markerOptions = new MarkerOptions();
           // String distance = "";
            //String duration = "";



            if(result.size()<1){
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }


            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                //lineOptions = new PolylineOptions();
                String dist = null;
                double dis = 0;


                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){	// Get distance from the list
                        distance = (String)point.get("distance");
                      if(distance.contains(" ")){
                          dist = distance.substring(0,distance.indexOf(" "));
                          dis = Double.parseDouble(dist) * 0.02;

                      }
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        String firstword = null;
                        if(duration.contains(" ")){
                            firstword = duration.substring(0,duration.indexOf(" "));
                        }
                        if(!carDone) {
                            carDone=true;
                            car_time = Integer.parseInt(firstword);
                            car_time_tv.setText(duration);
                        }
                        else{
                            bus_time = Integer.parseInt(firstword);
                            bus_time_tv.setText(duration);
                            carDone = false;
                            if(bus_time - car_time < 10){
                                recommendation_tv.setText("We recommend taking the public transit. You can reduce "
                                        + dis +" Ton CO2 emission.");
                            }else
                            {
                                recommendation_tv.setText("Please consider taking the public transit to help reduce "
                                        +dis+" Ton CO2 emission. The Earth Thanks You!");
                            }
                        }
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
            }
        }
    }


    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
      //  JSONObject jo = (JSONObject) adapterView.getItemAtPosition(position);
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
