package com.hiker.app.fragments;
//package info.androidhive.materialtabs.fragments;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
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
    private TextView lattitude;
    private TextView longitude;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
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
        lattitude = (TextView) view.findViewById(R.id.lattitude);
        longitude = (TextView) view.findViewById(R.id.longitude);
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
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
                //Log.d("accelero X",Float.toString( event.values[0]));
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                //Log.d("magnetic X",Float.toString( event.values[0]));
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
                if (mLastAccelerometerSet && mLastMagnetometerSet) {
                    Log.d("magnetic X",Float.toString( event.values[0]));
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
            /*
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                Log.d("magnetic X",Float.toString( event.values[0]));
                Log.d("magnnetic Y",Float.toString( event.values[1]));
                Log.d("magnetic Z",Float.toString( event.values[2]));
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Log.d("accelero X",Float.toString( event.values[0]));
                Log.d("accelero Y",Float.toString( event.values[1]));
                Log.d("accelero Z",Float.toString( event.values[2]));
            }*/
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}