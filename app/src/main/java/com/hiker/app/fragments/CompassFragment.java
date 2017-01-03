package com.hiker.app.fragments;
//package info.androidhive.materialtabs.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.SensorEventListener;

import com.hiker.app.R;


import static android.content.Context.SENSOR_SERVICE;




public class CompassFragment extends Fragment implements SensorEventListener {
    private ImageView iv_bousole;

    private TextView textLatitude;
    private TextView textLongitude;

    private LocationManager locationManager;
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
            textLatitude.setText("Lat: " + String.valueOf(location.getLatitude()));
            textLongitude.setText("Lon: " + String.valueOf(location.getLongitude()));
        }
    };

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;
    private SensorManager sensorManager = null;

    public CompassFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compass, container, false);
        super.onCreate(savedInstanceState);

        iv_bousole = (ImageView)view.findViewById(R.id.bousole);
        textLatitude = (TextView) view.findViewById(R.id.textLattitude);
        textLongitude = (TextView) view.findViewById(R.id.textLongitude);
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);

        locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE); //TODO Check permissions
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, onLocationChange);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, onLocationChange);

        // Register magnetic sensor
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
                if (mLastAccelerometerSet && mLastMagnetometerSet) {
                    Log.d("magnetic X", Float.toString(event.values[0]));
                    SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                    SensorManager.getOrientation(mR, mOrientation);
                    float azimuthInRadians = mOrientation[0];
                    float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
                    RotateAnimation ra = new RotateAnimation(
                            mCurrentDegree,
                            -azimuthInDegress,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF,
                            0.5f);

                    ra.setDuration(250);

                    ra.setFillAfter(true);

                    iv_bousole.startAnimation(ra);
                    mCurrentDegree = -azimuthInDegress;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}