package com.moremoregreen.androidnearbyplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
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
import com.moremoregreen.androidnearbyplaces.Model.MyPlaces;
import com.moremoregreen.androidnearbyplaces.Model.Results;
import com.moremoregreen.androidnearbyplaces.Remote.IGoogleAPIService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback {

    private static final int MY_PERMISSION_CODE = 1000;
    private GoogleMap mMap;

    private double latitude, longitude;
    private Location mLastLocation;
    private Marker mMarker;


    IGoogleAPIService mService;

    MyPlaces currentPlace;

    //new Location
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Init Service
        //不知道為什麼COMMON前面那個一定要有
        mService = com.moremoregreen.androidnearbyplaces.Common.getGoogleAPIService();

        //Request Runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_hospital:
                        nearByPlace("hospital");
                        break;
                    case R.id.action_market:
                        nearByPlace("market");
                        break;
                    case R.id.action_restaurant:
                        nearByPlace("restaurant");
                        break;
                    case R.id.action_school:
                        nearByPlace("school");
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        //init locaion
        buildLocationCallBack();
        buildLocationRequest();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());

    }

    @Override
    protected void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            //Ctrl + O

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();

                if (mMarker != null) {
                    mMarker.remove();
                }
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                LatLng latLng = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title("你的位置")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mMarker = mMap.addMarker(markerOptions);

                //Move Camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
            }
        };
    }

    private void buildLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setSmallestDisplacement(10f);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void nearByPlace(final String placeType) {
        mMap.clear();
        String url = getUrl(latitude, longitude, placeType);

        mService.getNearByPlaces(url)
                .enqueue(new Callback<MyPlaces>() {
                    @Override
                    public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {

                        currentPlace = response.body(); // 記得要給currentPlace一個值

                        if (response.isSuccessful()) {
                            for (int i = 0; i < response.body().getResults().length; i++) {
                                MarkerOptions markerOptions = new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i];
                                double lat = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double lng = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                                String placeName = googlePlace.getName();
                                String vicinity = googlePlace.getVicinity();
                                LatLng latLng = new LatLng(lat, lng);
                                markerOptions.position(latLng);
                                markerOptions.title(placeName);
                                if (placeType.equals("hospital"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_hospital));
                                else if (placeType.equals("market"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_shopping));
                                else if (placeType.equals("restaurant"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_restaurant));
                                else if (placeType.equals("school"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_school));
                                else
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                markerOptions.snippet(String.valueOf(i)); //Assign index for marker
                                //Add to map
                                mMap.addMarker(markerOptions);

                                //Move Camera
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyPlaces> call, Throwable t) {

                    }
                });
    }

    private String getUrl(double latitude, double longitude, String placeType) {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + 10000);
        googlePlaceUrl.append("&type=" + placeType);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + getResources().getString(R.string.browser_key));
        Log.d("GetUrl", googlePlaceUrl.toString());
        return googlePlaceUrl.toString();
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, new String[]{

                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSION_CODE);
            else
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSION_CODE);
            return false;
        } else
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        mMap.setMyLocationEnabled(true);

                        //init locaion
                        buildLocationCallBack();
                        buildLocationRequest();

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
                    }
                }

            }

            break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Init Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                mMap.setMyLocationEnabled(true);
            }
        } else {

            mMap.setMyLocationEnabled(true);
        }
        //Make event click on Marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getSnippet() != null) {
                    //當User選了一個marker,get Result of Place and assign to static variable
                    com.moremoregreen.androidnearbyplaces.Common.currentResults = currentPlace.getResults()[Integer.parseInt(marker.getSnippet())];
                    //start new activity
                    startActivity(new Intent(MapsActivity.this, ViewPlace.class));
                }
                return true;
            }
        });
    }

}

