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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by hariharanvenkatramanan on 3/18/17.
 */

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.navigation_main_layout);
        LocationUtils.startLocationUpdates(this);
        final Context c = this;
        final String TAG = "Navigation Activity";

        final LinearLayout results = (LinearLayout)findViewById(R.id.linearLayout_results);
        final Geocoder geocoder = new Geocoder(getApplicationContext());
       // final EditText from_loc_edittext = (EditText)findViewById(R.id.autoCompleteTextView_from);
        final EditText to_loc_edittext = (EditText)findViewById(R.id.autoCompleteTextView_to);
        final TextView car_time_tv = (TextView)findViewById(R.id.car_timing);
        final TextView bus_time_tv = (TextView)findViewById(R.id.bus_timing);
        final TextView recommendation_tv = (TextView)findViewById(R.id.textView_recommendation);

        final String car_time = "";
        final String bus_time = "";

        ImageButton button_loc = (ImageButton) findViewById(R.id.getLocationButton);
        ImageButton button_send = (ImageButton) findViewById(R.id.sendButton);


        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.fragment_from);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        results.setVisibility(View.INVISIBLE);

        button_loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double lat = LocationUtils.getLat();
                double lon = LocationUtils.getLon();
                try {
                    List<Address> address = geocoder.getFromLocation(lat,lon,1);
                    Address a = address.get(0);
                    autocompleteFragment.setText(a.getAddressLine(0).toString()+a.getAddressLine(1).toString());
                  //  from_loc_edittext.setText(a.getAddressLine(0).toString()+a.getAddressLine(1).toString());

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
                if(to_loc_edittext.getText().toString().isEmpty()){
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



    @Override
    public void onResume() {
        super.onResume();
        LocationUtils.startLocationUpdates(this);

    }
}
