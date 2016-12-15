package com.hiker.app.fragments;
//package info.androidhive.materialtabs.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hiker.app.R;

//import info.androidhive.materialtabs.R;


public class CompassFragment extends Fragment {
    TextView textViewLat;
    TextView textViewLon;

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

        textViewLat = (TextView)view.findViewById(R.id.textViewLat);
        textViewLon = (TextView)view.findViewById(R.id.textViewLon);

        return view;
    }

    public void updateCoordinates(double lat, double lon) {
        textViewLat.setText(String.valueOf(lat));
        textViewLon.setText(String.valueOf(lon));
    }
}