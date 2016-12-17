package com.hiker.app.services;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.hiker.app.utils.Constants;
import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.utils.State;

public class TrackerService extends Service implements SensorEventListener {
    private MyStorageManager myStorageManager;

    private long trackId = -1;

    private Location lastPoint;

    private float distance = 0;
    private int stepsStart = -1;
    private int steps = 0;

    private Handler handler = new Handler();
    private Runnable runnable;
    private int delay = 10000;

    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;

    private LocationManager locationMgr;
    private LocationListener onLocationChange = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double altitude = location.getAltitude();

            float speed = location.getSpeed();

            Location point = new Location(location);

            if (lastPoint == null) lastPoint = point;
            else {
                distance += lastPoint.distanceTo(point);
                lastPoint = point;
            }

            /*Toast.makeText(getBaseContext(),
                    "Voici les coordonnées de votre téléphone : " + distance + " " + speed + " " + altitude,
                    Toast.LENGTH_LONG).show();*/

            myStorageManager.insertPoint(trackId, latitude, longitude, altitude, speed);

            //Envoie d'une notification indiquant que la BDD a été mise à jour
            sendBroadcastUpdated(Constants.EXTRA_POINTS_UPDATED);
        }
    };

    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        float[] values = event.values;

        if (values.length > 0) {
            if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                if (stepsStart == -1) stepsStart = (int)values[0];
                else steps = (int)values[0] - stepsStart;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void sendBroadcastUpdated(String extra) {
        Intent localIntent = new Intent(Constants.ACTION_BROADCAST).putExtra(extra, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //TODO: Change Name
        myStorageManager = new MyStorageManager(getApplicationContext());

        trackId = myStorageManager.insertTrack("TestDate", SystemClock.elapsedRealtime());

        //Mise à jour des informations globales
        State.setServiceTrackerState(true);
        State.setCurrentSession(trackId);

        //TODO: Gestion permissions / Pour le moment l'application va crash si la permission n'est pas accordée
        locationMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, onLocationChange);
        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, onLocationChange);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);

        handler.postDelayed(new Runnable(){
            public void run(){
                if (State.getCurrentSession() != -1) {
                    myStorageManager.updateTrack(State.getCurrentSession(), SystemClock.elapsedRealtime(), (int)distance, steps);
                    sendBroadcastUpdated(Constants.EXTRA_TRACK_UPDATED);
                }

                //Repeat
                runnable = this;
                handler.postDelayed(runnable, delay);
            }
        }, delay);

        sendBroadcastUpdated(Constants.EXTRA_SESSION_START);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);

        locationMgr.removeUpdates(onLocationChange);

        mSensorManager.unregisterListener(this, mStepCounterSensor);

        myStorageManager.updateTrack(State.getCurrentSession(), SystemClock.elapsedRealtime(), (int)distance, steps);

        State.setServiceTrackerState(false);
        State.setCurrentSession(-1);

        sendBroadcastUpdated(Constants.EXTRA_SESSION_STOP);
    }
}
