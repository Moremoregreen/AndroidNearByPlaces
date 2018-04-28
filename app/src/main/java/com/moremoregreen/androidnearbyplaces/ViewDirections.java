package com.moremoregreen.androidnearbyplaces;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

//import com.moremoregreen.androidnearbyplaces.Model.Location;  移除這個

import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.moremoregreen.androidnearbyplaces.Helper.DirectionJSONParser;
import com.moremoregreen.androidnearbyplaces.Remote.IGoogleAPIService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewDirections extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Location mLastLocation;
    Marker mCurrentMarker;

    Polyline polyline;

    IGoogleAPIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_directions);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mService = Common.getGoogleAPIServiceScalars();

        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            //Ctrl + O

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title("你的位置")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mCurrentMarker = mMap.addMarker(markerOptions);

                //Move Camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12f));

                //Create Marker for destination locaion
                LatLng destinationLatLng = new LatLng(Double.parseDouble(Common.currentResults.getGeometry().getLocation().getLat()),
                        Double.parseDouble(Common.currentResults.getGeometry().getLocation().getLng()));


                mMap.addMarker(  new MarkerOptions()
                        .position(destinationLatLng)
                        .title(Common.currentResults.getName())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                drawPath(mLastLocation, Common.currentResults.getGeometry().getLocation());
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                        .title("你的位置")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mCurrentMarker = mMap.addMarker(markerOptions);

                //Move Camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(12f));

                //Create Marker for destination locaion
                LatLng destinationLatLng = new LatLng(Double.parseDouble(Common.currentResults.getGeometry().getLocation().getLat()),
                        Double.parseDouble(Common.currentResults.getGeometry().getLocation().getLng()));


                  mMap.addMarker(  new MarkerOptions()
                          .position(destinationLatLng)
                          .title(Common.currentResults.getName())
                          .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                  drawPath(mLastLocation, Common.currentResults.getGeometry().getLocation());
            }
        });
    }

    private void drawPath(Location mLastLocation, com.moremoregreen.androidnearbyplaces.Model.Location location) {
        //Clear all polyline
        if (polyline != null) {
            polyline.remove();
        }
        String origin = new StringBuilder(String.valueOf(mLastLocation.getLatitude())).append(",")
                .append(String.valueOf(mLastLocation.getLongitude()))
                .toString();
        String destination = new StringBuilder(location.getLat()).append(",").append(location.getLng()).toString();

        mService.getDiretions(origin,destination)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        new ParserTask().execute(response.body().toString());
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
    }
        //Dialog 用 github的Lib
        //https://github.com/d-max/spots-dialog
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>>{
        AlertDialog waitingDialog = new SpotsDialog(ViewDirections.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waitingDialog.show();
            waitingDialog.setMessage("請稍等...");
        }

        @Override
        //使用DirectionsJSONParser.java in github
        //https://github.com/gripsack/android/blob/master/app/src/main/java/com/github/gripsack/android/data/model/DirectionsJSONParser.java
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jsonObject);
            }catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i = 0; i < lists.size(); i++)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);
                for (int j = 0; j < path.size(); j++)
                {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            polyline = mMap.addPolyline(polylineOptions);
            waitingDialog.dismiss();
        }
    }
}
