package com.example.yerevanguideapp;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.app.Dialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = "MainActivity";

    //vars
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 16f;
    public static final float MINIMAL_ZOOM = 11f;
    public static long MAP_REFRESH_TIME = 120000;


    private boolean mLocationPermissionGranted = false;
    private GlobalClass glClass;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProvider;
    private ClosePlaceReceiver mReceiver;
    public ListView nearbyPlaces;
    public CountDownTimer mapRefreshTimer;

    //Current sources
    public String[] places;
    public String[] descriptions;
    public String[] placeTypes;
    public String[] coordLat;
    public String[] coordLng;
    public List<LatLng> coordinates;

    public List<Marker> markers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //userPreferences = new UserPreferences();
        //userPreferences.setOthersChecked(false);

        Resources res = getResources();
        glClass = (GlobalClass) getApplicationContext();
        nearbyPlaces = (ListView) findViewById(R.id.nearbyPlaces);
        places = res.getStringArray(R.array.places);
        descriptions = res. getStringArray(R.array.descriptions);
        placeTypes = res.getStringArray(R.array.place_type);
        coordLat = res.getStringArray(R.array.coordinates_lat);
        coordLng = res.getStringArray(R.array.coordinates_lng);
        coordinates = new ArrayList<LatLng>();

        mReceiver = new ClosePlaceReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(LocationService.CLOSE_PLACE_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

        //Making a LatLng list out of coordinates(1)
        for(int i = 0; i < places.length; i++)
        {
            coordinates.add(new LatLng(Double.valueOf(coordLat[i]), Double.valueOf(coordLng[i])));
        }
        Log.d(TAG, "onCreate: list created");
        //end(1)

        if (isServiceOk()) {
            Toast.makeText(this, "Everything is OK", Toast.LENGTH_SHORT).show();
        }

        getLocationPermission();
        refreshMapMarkers();
        makeList();
    }

    //Used to be a button for displaying user location
    private void viewUserData()
    {
        if(glClass.getUserLocationLAT() == 0.0d && glClass.getUserLocationLNG() == 0.0d)
        {
            showMessage("Error", "No data found");
            return;
        }

        StringBuilder buffer = new StringBuilder(); //used to be StringBuffer

        buffer.append("CURRENT USER LOCATION:" + "\n");
        buffer.append("LAT: " + glClass.getUserLocationLAT() + "\n");
        buffer.append("LNG: " + glClass.getUserLocationLNG() + "\n\n");

        showMessage("Data", buffer.toString());
    }
    public void refreshMapMarkers()
    {
        mapRefreshTimer = new CountDownTimer(MAP_REFRESH_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        };
    }
    private void makeList()
    {
        ItemAdapter itemAdapter = new ItemAdapter(this, places, descriptions);
        nearbyPlaces.setAdapter(itemAdapter);
        nearbyPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                moveCamera(new LatLng(Double.valueOf(coordLat[position]), Double.valueOf(coordLng[position])),
                        DEFAULT_ZOOM);
                //Toast.makeText(MainActivity.this, "Item number " + position + " was clicked", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void showMessage(String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public boolean isServiceOk() {
        Log.d(TAG, "isServiceOk: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            //GOOGLE PLAY IS AVAILABLE
            Log.d(TAG, "isServiceOk: Google Play services are working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //GOOGLE PLAY IS NOT AVAILABLE, BUT CAN FIX
            Log.d(TAG, "isServiceOk: an error occured, but it's fixable");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,
                    available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            //GOOGLE PLAY IS UNAVAILABLE
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the device's current location");

        mFusedLocationProvider = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {

                Task location = mFusedLocationProvider.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Location found");
                            Location currentLocation = (Location) task.getResult();
                            glClass.setUserLocationLAT(currentLocation.getLatitude());
                            glClass.setUserLocationLNG(currentLocation.getLongitude());
                            moveCamera(new LatLng(glClass.getUserLocationLAT(), glClass.getUserLocationLNG()),
                                    DEFAULT_ZOOM);

                            startLocationService();
                        } else {
                            Log.d(TAG, "onComplete: Location not found");
                            Toast.makeText(MainActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());

        }
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                MainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.yerevanguideapp.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void moveCamera(LatLng latLng, float zoom) {
        if(latLng.latitude == 0 && latLng.longitude == 0)
        {
            Log.d(TAG, "moveCamera: wrong coordinates given");
            return;
        }
        Log.d(TAG, "moveCamera: moving camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        for(int i = 0; i < markers.size(); i++)
        {
            if(latLng.equals(markers.get(i).getPosition()))
            {
                markers.get(i).showInfoWindow();
            }
        }


        /*MarkerOptions mark = new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(mark);*/
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(MainActivity.this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        //Map style
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.mapstyle));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);

            LatLng yerevanBottomLeftBound = new LatLng(40.054627, 44.329105);
            LatLng yerevanTopRightBound = new LatLng(40.281098, 44.718621);
            LatLngBounds YEREVAN = new LatLngBounds(yerevanBottomLeftBound, yerevanTopRightBound);
            mMap.setLatLngBoundsForCameraTarget(YEREVAN);
            mMap.setMinZoomPreference(MINIMAL_ZOOM);
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(MainActivity.this, PlaceInfoActivity.class);
                    intent.putExtra("ITEM_PLACE_NAME", marker.getTitle());
                    startActivity(intent);
                }
            });

            MakeMarkers(coordinates, places, descriptions);
        }

    }

    private void MakeMarkers(List<LatLng> coords, String[] names, String[] desc){
        Log.d(TAG, "MakeMarkers: was called, also " + coords.size() + " <- the size");

        //testing user location mark
        /*Cursor curs = mDb.getAllData();
        curs.moveToNext();
        LatLng userLocation = new LatLng(Double.parseDouble(curs.getString(1)),
                Double.parseDouble(curs.getString(2)));
        MarkerOptions mark;
        mark = new MarkerOptions()
                .position(userLocation)
                .title("You are here");
        mMap.addMarker(mark);*/

        MarkerOptions mark;
        markers = new ArrayList<Marker>();
        for(int i = 0; i < coords.size(); i++){
             mark = new MarkerOptions()
                 .position(coords.get(i))
                 .title(names[i])
                 .snippet(desc[i]);
             Marker mMarker = mMap.addMarker(mark);
             markers.add(mMarker);
            Log.d(TAG, "MakeMarkers: marker number " + i + " initialized");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0)
                {
                    for(int i = 0; i< grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    //INITMAP
                    initMap();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try{
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
        }
        Log.d(TAG, "onStop: receiver unregistered");
        super.onDestroy();
    }

    private class ClosePlaceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double placeLat = intent.getDoubleExtra("PLACE_LAT", 0);
            double placeLng = intent.getDoubleExtra("PLACE_LNG", 0);
            moveCamera(new LatLng(placeLat, placeLng), DEFAULT_ZOOM+2f);
            Log.d(TAG, "onReceive: broadcast received");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_prefs:
                Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(intent);
                return true;
            //case R.id.item_userloc:
            //    viewUserData();
            //   return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }
}


/* UserLocButton from options_menu.xml
<item android:id="@+id/item_userloc"
        android:title="User Location"
        app:showAsAction="never"/>
 */
