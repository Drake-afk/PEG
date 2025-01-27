package com.example.yerevanguideapp;

import android.app.PendingIntent;
import android.app.Service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;

import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LocationService extends Service{

    private static final String TAG = "LocationService";

    private FusedLocationProviderClient mFusedLocationClient;
    private final static long UPDATE_INTERVAL = 4 * 1000;  /* 4 secs */
    private final static long FASTEST_INTERVAL = 2000; /* 2 sec */
    public static final String CHANNEL_ID = "nc_placepush";
    public static final String CHANNEL_ID_2 = "nc_bgprocess";
    public static final String CHANNEL_NAME = "Close-by Places";
    public static final String CHANNEL_NAME_2 = "Background Process";
    public static final String CHANNEL_DESC = "NC Description";
    public static final String CLOSE_PLACE_ACTION = "CLOSE_PLACE_ACTION";

    private GlobalClass glClass;
    private boolean isInTheArea;
    public String[] places;
    public String[] placeTypes;
    public String[] coordLat;
    public String[] coordLng;

    public final double proximity = 0.0010; // 112 metres

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            channel.setLightColor(Color.YELLOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        glClass = (GlobalClass) getApplicationContext();
        isInTheArea = false;

        Resources res = getResources();
        places = res.getStringArray(R.array.places);
        coordLat = res.getStringArray(R.array.coordinates_lat);
        coordLng = res.getStringArray(R.array.coordinates_lng);
        placeTypes = res.getStringArray(R.array.place_type);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_2,CHANNEL_NAME_2, NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("")
                    .setChannelId(CHANNEL_ID_2)
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");
        getLocation();
        return START_NOT_STICKY;
    }

    private void getLocation() {
        // Create the location request to start receiving updates
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: stopping the location service.");
            stopSelf();
            return;
        }
        Log.d(TAG, "getLocation: getting location information.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Log.d(TAG, "onLocationResult: got location result.");
                        Location currentLocation = locationResult.getLastLocation();

                        glClass.setUserLocationLAT(currentLocation.getLatitude());
                        glClass.setUserLocationLNG(currentLocation.getLongitude());

                        checkClosePlaces();
                    }
                },
                Looper.myLooper()); // Looper.myLooper tells this to repeat forever until thread is destroyed
    }
    
    private void checkClosePlaces()
    {
        LatLng curLoc = new LatLng(glClass.getUserLocationLAT(), glClass.getUserLocationLNG());

        for (int i = 0; i < coordLng.length; ++i)
        {
            LatLng place = new LatLng(Double.valueOf(coordLat[i]),
                    Double.valueOf(coordLng[i]));
            if(areClose(curLoc, place) && !isInTheArea)
            {
                if (prefCheck(placeTypes[i]))
                {
                    displayNotification(places[i]);
                    Intent closePlaceBroadcast = new Intent();
                    closePlaceBroadcast.setAction(CLOSE_PLACE_ACTION);
                    closePlaceBroadcast.putExtra("PLACE_LAT", place.latitude);
                    closePlaceBroadcast.putExtra("PLACE_LNG", place.longitude);
                    sendBroadcast(closePlaceBroadcast);

                    isInTheArea = true;
                    Log.d(TAG, "checkClosePlaces: You just got close to something");
                    break;
                }

            }
            else if (areClose(curLoc, place) && isInTheArea)
            {
                Log.d(TAG, "checkClosePlaces: You are close to something");
                if(!prefCheck(placeTypes[i]))
                {
                    isInTheArea = false;
                }
                break;
            }
            else if(i == coordLng.length-1 && isInTheArea)
            {
                isInTheArea = false;
            }
            else if(i == coordLng.length-1 && !isInTheArea) {
                Log.d(TAG, "checkClosePlaces: No close places");
            }
        }
    }

    private boolean prefCheck(String placeType) {
        SharedPreferences sharedPreferences = getSharedPreferences(PreferencesActivity.SHARED_PREFS, MODE_PRIVATE);

        switch (placeType) {
            case "MUSEUM": if(sharedPreferences.getBoolean(PreferencesActivity.PREFS_MUSEUMS, false)){
                return true;
            }
            case "MONUMENT": if(sharedPreferences.getBoolean(PreferencesActivity.PREFS_MONUMENTS, false)){
                return true;
            }
            case "GALLERY": if(sharedPreferences.getBoolean(PreferencesActivity.PREFS_GALLERIES, false)){
                return true;
            }
            case "PARK": if(sharedPreferences.getBoolean(PreferencesActivity.PREFS_PARKS, false)){
                return true;
            }
            case "OTHER": if(sharedPreferences.getBoolean(PreferencesActivity.PREFS_OTHERS, false)){
                return true;
            }
            default: return false;
        }
    }

    private boolean areClose(LatLng a, LatLng b)
    {
        double dist = Math.pow(a.longitude - b.longitude, 2)
                + Math.pow(a.latitude - b.latitude, 2);
        if (dist <= Math.pow(proximity,2)){
            return true;
        }
        return false;
    }

    private void displayNotification(String placeName)
    {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingResultIntend = PendingIntent.getActivity(this,
                1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_interesting_place_24dp)
                .setContentTitle("Look around!")
                .setContentIntent(pendingResultIntend)
                .setAutoCancel(true)
                .setChannelId(CHANNEL_ID)
                .setContentText("There is an interesting place nearby: " + placeName)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.notify(2, mBuilder.build());
    }


}
