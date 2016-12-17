package com.hiker.app.fragments;
//package info.androidhive.materialtabs.fragments;

import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.R;
import com.hiker.app.utils.State;
import com.hiker.app.utils.Utils;

//import info.androidhive.materialtabs.R;


public class SessionFragment extends Fragment {
    private MyStorageManager myStorageManager;

    private SimpleDateFormat sdf;

    private View view;

    private TextView noSession;

    private Chronometer time;
    private TextView distance;
    private TextView speed;
    private TextView altitude;
    private TextView up;
    private TextView down;
    private TextView steps;

    public SessionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myStorageManager = new MyStorageManager(getActivity().getApplicationContext());

        view = inflater.inflate(R.layout.fragment_session, container, false);
        noSession = (TextView)view.findViewById(R.id.textViewNoSession);

        setup();

        return view;
    }

    private void setup() {
        View v = view.findViewById(R.id.itemTime);
        ((TextView)v.findViewById(R.id.textTitle)).setText("Temps");
        time = ((Chronometer)v.findViewById(R.id.chronometer));
        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer cArg) {
                cArg.setText(Utils.dateToString(SystemClock.elapsedRealtime() - cArg.getBase()));
            }
        });

        v = view.findViewById(R.id.itemDistance);
        ((TextView)v.findViewById(R.id.textTitle)).setText("Distance");
        ((TextView)v.findViewById(R.id.textUnit)).setText("m");
        distance = ((TextView)v.findViewById(R.id.textValue));
        distance.setText("0");

        v = view.findViewById(R.id.itemSpeed);
        ((TextView)v.findViewById(R.id.textTitle)).setText("Altitude");
        ((TextView)v.findViewById(R.id.textUnit)).setText("m");
        speed = ((TextView)v.findViewById(R.id.textValue));
        speed.setText("0");

        v = view.findViewById(R.id.itemAltitude);
        ((TextView)v.findViewById(R.id.textTitle)).setText("Allure");
        ((TextView)v.findViewById(R.id.textUnit)).setText("km/h");
        altitude = ((TextView)v.findViewById(R.id.textValue));
        altitude.setText("0");

        v = view.findViewById(R.id.itemUp);
        ((TextView)v.findViewById(R.id.textTitle)).setText("Distance montée");
        ((TextView)v.findViewById(R.id.textUnit)).setText("m");
        up = ((TextView)v.findViewById(R.id.textValue));
        up.setText("0");

        v = view.findViewById(R.id.itemDown);
        ((TextView)v.findViewById(R.id.textTitle)).setText("Distance descendue");
        ((TextView)v.findViewById(R.id.textUnit)).setText("m");
        down = ((TextView)v.findViewById(R.id.textValue));
        down.setText("0");

        v = view.findViewById(R.id.itemSteps);
        ((TextView)v.findViewById(R.id.textTitle)).setText("Pas");
        ((TextView)v.findViewById(R.id.textUnit)).setText("p");
        steps = ((TextView)v.findViewById(R.id.textValue));
        steps.setText("0");
    }

    public void update() {  //TODO Ascend / Descend
        if (State.getCurrentSession() != -1) {
            noSession.setVisibility(View.GONE);

            if (myStorageManager != null) {
                Cursor cursor = myStorageManager.getTrack(State.getCurrentSession());
                if (cursor != null) {
                    if (cursor.getCount() == 1) {
                        cursor.moveToFirst();

                        int d = cursor.getInt(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_DISTANCE));
                        if (d < 1000) ((TextView)view.findViewById(R.id.itemDistance).findViewById(R.id.textUnit)).setText("m");
                        else {
                            ((TextView)view.findViewById(R.id.itemDistance).findViewById(R.id.textUnit)).setText("km");
                            d = (int)((float)d/1000);
                        }
                        distance.setText(String.valueOf(d));

                        steps.setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_STEPS)));
                    }
                }
                cursor = myStorageManager.getPointsOfTrack(State.getCurrentSession());
                if (cursor != null) {
                    if (cursor.getCount() > 0) {
                        cursor.moveToLast();
                        speed.setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_SPEED)));
                        altitude.setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_ALTITUDE)));
                    }
                }
            }
        }
        else {
            noSession.setVisibility(View.VISIBLE);
            time.setText("0:00:00");

            distance.setText("0");
            ((TextView)view.findViewById(R.id.itemDistance).findViewById(R.id.textUnit)).setText("m");

            altitude.setText("0");
            speed.setText("0");
            steps.setText("0");
        }
    }

    public void startChronometer() {
        time.setBase(SystemClock.elapsedRealtime());
        time.start();

        noSession.setVisibility(View.GONE);
    }

    public void stopChronometer() {
        time.stop();
    }
}