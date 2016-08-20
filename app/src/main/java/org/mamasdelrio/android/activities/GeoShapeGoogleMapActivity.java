/*
 * Copyright (C) 2016 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.spatial.MapHelper;
import org.odk.collect.android.widgets.GeoShapeWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Version of the GeoShapeGoogleMapActivity that uses the new Maps v2 API and Fragments to enable
 * specifying a location via placing a tracker on a map.
 *
 * @author jonnordling@gmail.com
 *
 */

public class GeoShapeGoogleMapActivity extends FragmentActivity implements LocationListener, OnMarkerDragListener, OnMapLongClickListener {

    private SharedPreferences sharedPreferences;

    private GoogleMap mMap;
    private String basemap;
    private UiSettings gmapSettings;
    private LocationManager mLocationManager;
    private Boolean mGPSOn = false;
    private Boolean mNetworkOn =false;
    private Location curLocation;
    private LatLng curlatLng;
    private Boolean initZoom = false;
    private PolygonOptions polygonOptions;
    private Polygon polygon;
    private ArrayList<LatLng> latLngsArray = new ArrayList<LatLng>();
    private ArrayList<Marker> markerArray = new ArrayList<Marker>();
    private Button gps_button;
    private Button clear_button;
    private Button polygon_button;
    private Button return_button;
    private Button layers_button;
    private String final_return_string;
    private Boolean data_loaded = false;
    private Boolean clear_button_test;
    private MapHelper mHelper;
    private AlertDialog zoomDialog;
    private View zoomDialogView;
    private Button zoomPointButton;
    private Button zoomLocationButton;
    private Boolean foundFirstLocation = false;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.geoshape_google_layout);


        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap)).getMap();
        mHelper = new MapHelper(this,mMap);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(Color.RED);

        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                mGPSOn = true;
                curLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                mNetworkOn = true;
                curLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        if(!mGPSOn & !mNetworkOn){
            showGPSDisabledAlertToUser();
        }

        gps_button = (Button)findViewById(R.id.gps);
        gps_button.setEnabled(false);
        gps_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(curLocation !=null){
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng,16));
//                }
                showZoomDialog();
            }
        });

        clear_button = (Button) findViewById(R.id.clear);
        clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerArray.size() != 0){
                    showClearDialog();
                }
            }
        });
        return_button = (Button) findViewById(R.id.save);
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnLocation();
            }
        });


        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if ( intent.hasExtra(GeoShapeWidget.SHAPE_LOCATION) ) {
                data_loaded =true;
                clear_button.setEnabled(true);
                String s = intent.getStringExtra(GeoShapeWidget.SHAPE_LOCATION);
                gps_button.setEnabled(true);
                overlayIntentPolygon(s);
            }
        }


        layers_button = (Button)findViewById(R.id.layers);
        layers_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mHelper.showLayersDialog();

            }
        });

        zoomDialogView = getLayoutInflater().inflate(R.layout.geoshape_zoom_dialog, null);

        zoomLocationButton = (Button) zoomDialogView.findViewById(R.id.zoom_location);
        zoomLocationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(curLocation !=null){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curlatLng,17));
                }
                zoomDialog.dismiss();
            }
        });

        zoomPointButton = (Button) zoomDialogView.findViewById(R.id.zoom_shape);
        zoomPointButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                zoomToCentroid();
                zoomtoBounds();
                zoomDialog.dismiss();
            }
        });
        // If there is a last know location go there
        if(curLocation !=null){
            curlatLng = new LatLng(curLocation.getLatitude(),curLocation.getLongitude());
            foundFirstLocation = true;
            initZoom = true;
            gps_button.setEnabled(true);
            showZoomDialog();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mHelper.setBasemap();
        List<String> providers = mLocationManager.getProviders(true);

        for (String provider : providers) {
            if (provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                mGPSOn = true;
                curLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//                gps_button.setEnabled(true);
            }
            if (provider.equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)) {
                mNetworkOn = true;
                curLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
//                gps_button.setEnabled(true);
            }
        }

        if (mGPSOn) {
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            gps_button.setEnabled(true);
        }
        if (mNetworkOn) {
//            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            gps_button.setEnabled(true);
        }

    }
    @Override
    protected void onStart() {
    	super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this);
    }

    @Override
    protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this);
    	super.onStop();
    }


    private void returnLocation(){
        final_return_string = generateReturnString();
        Intent i = new Intent();
        i.putExtra(
                FormEntryActivity.GEOSHAPE_RESULTS,
                final_return_string);
        setResult(RESULT_OK, i);
        finish();
    }

    private void overlayIntentPolygon(String str){
        mMap.setOnMapLongClickListener(null);
        clear_button.setEnabled(true);
        clear_button_test = true;
        String s = str.replace("; ",";");
        String[] sa = s.split(";");
        for (int i=0;i<(sa.length -1 );i++){
            String[] sp = sa[i].split(" ");
            double gp[] = new double[4];
            String lat = sp[0].replace(" ", "");
            String lng = sp[1].replace(" ", "");
            gp[0] = Double.parseDouble(lat);
            gp[1] = Double.parseDouble(lng);
            LatLng point = new LatLng(gp[0], gp[1]);
            polygonOptions.add(point);
            MarkerOptions mMarkerOptions = new MarkerOptions().position(point).draggable(true);
            Marker marker= mMap.addMarker(mMarkerOptions);
            markerArray.add(marker);
        }
        polygon = mMap.addPolygon(polygonOptions);
        update_polygon();

    }

    private String generateReturnString() {
        String temp_string = "";
        //Add the first marker to the end of the array, so the first and the last are the same
        if (markerArray.size() > 1 ){
            markerArray.add(markerArray.get(1));
            for (int i = 0 ; i < markerArray.size();i++){
                String lat = Double.toString(markerArray.get(i).getPosition().latitude);
                String lng = Double.toString(markerArray.get(i).getPosition().longitude);
                String alt ="0.0";
                String acu = "0.0";
                temp_string = temp_string+lat+" "+lng +" "+alt+" "+acu+";";
            }
        }
        return temp_string;
    }

    @Override
    public void onLocationChanged(Location location) {
        // If there is a location allow for user to be able to fly there
        gps_button.setEnabled(true);
        curLocation = location;
        curLocation = location;
        curlatLng = new LatLng(curLocation.getLatitude(),curLocation.getLongitude());
        if(!foundFirstLocation){
            showZoomDialog();
            foundFirstLocation = true;
        }


    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void update_polygon(){
        ArrayList<LatLng> tempLat =  new ArrayList<LatLng>();
        for (int i =0;i<markerArray.size();i++){
            LatLng latLng = markerArray.get(i).getPosition();
            tempLat.add(latLng);
        }
        latLngsArray = tempLat;
        polygon.setPoints(tempLat);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions mMarkerOptions = new MarkerOptions().position(latLng).draggable(true);
        Marker marker= mMap.addMarker(mMarkerOptions);
        markerArray.add(marker);

        if (polygon == null){
            clear_button.setEnabled(true);
            clear_button_test = true;
            polygonOptions.add(latLng);
            polygon = mMap.addPolygon(polygonOptions);
        }else{
            update_polygon();
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        update_polygon();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        update_polygon();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        update_polygon();
    }

    private void zoomtoBounds(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markerArray) {
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();
                int padding = 200; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            }
        }, 100);

    }


    private void clearFeatures(){
        mMap.clear();
        clear_button_test = false;
        polygon = null;
        polygonOptions = new PolygonOptions();
        polygonOptions.strokeColor(Color.RED);
        markerArray.clear();
        mMap.setOnMapLongClickListener(this);

    }
    private void showClearDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.geo_clear_warning))
                .setPositiveButton(getString(R.string.clear), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clearFeatures();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                }).show();

    }

    public void showZoomDialog() {
        if (zoomDialog == null) {
            AlertDialog.Builder p_builder = new AlertDialog.Builder(this);
            p_builder.setTitle(getString(R.string.zoom_to_where));
            p_builder.setView(zoomDialogView)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.cancel();
                            zoomDialog.dismiss();
                        }
                    });
            zoomDialog = p_builder.create();
        }

        if (curLocation!= null){
            zoomLocationButton.setEnabled(true);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomLocationButton.setTextColor(Color.parseColor("#ff333333"));
        }else{
            zoomLocationButton.setEnabled(false);
            zoomLocationButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomLocationButton.setTextColor(Color.parseColor("#FF979797"));
        }

        if (markerArray.size() != 0){
            zoomPointButton.setEnabled(true);
            zoomPointButton.setBackgroundColor(Color.parseColor("#50cccccc"));
            zoomPointButton.setTextColor(Color.parseColor("#ff333333"));
        }else{
            zoomPointButton.setEnabled(false);
            zoomPointButton.setBackgroundColor(Color.parseColor("#50e2e2e2"));
            zoomPointButton.setTextColor(Color.parseColor("#FF979797"));
        }
        zoomDialog.show();
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.gps_enable_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable_gps),
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                            }
                        });
        alertDialogBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
