package com.hiker.app.fragments;
//package info.androidhive.materialtabs.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hiker.app.utils.Constants;
import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.R;
import com.hiker.app.utils.State;

public class MapFragment extends Fragment {
    private MyStorageManager myStorageManager;

    private LocationManager locationManager;

    private GoogleMap googleMap;

    private MapView mMapView;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myStorageManager = new MyStorageManager(getActivity().getApplicationContext());

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                setMyLocationEnable(true);
            }
        });

        return view;
    }

    public void zoomOnCurrentLocation() {
        if (!googleMap.isMyLocationEnabled())
            setMyLocationEnable(true);
        else {
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestNeededPermissions();
                } else {
                    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    //Zoom to current Location
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        }
    }

    public void clearPath() {
        googleMap.clear();
    }

    public void updatePath() {
        if (getCurrentSession() != -1) {
                if (myStorageManager != null && googleMap != null) {
                Cursor cursor = myStorageManager.getPointsOfTrack(getCurrentSession());

                if (cursor != null) {
                    if (cursor.getCount() > 1) {
                        PolylineOptions line = new PolylineOptions().width(20).color(Color.RED);

                        cursor.moveToFirst();

                        LatLng prev = new LatLng(cursor.getDouble(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_LONGITUDE)));
                        cursor.moveToNext();

                        while (!cursor.isAfterLast()) {
                            LatLng current = new LatLng(cursor.getDouble(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_LONGITUDE)));
                            line.add(prev, current);

                            cursor.moveToNext();
                            prev = current;
                        }

                        clearPath();
                        googleMap.addPolyline(line);
                    }
                }
            }
        } else clearPath(); //TODO: réfléchir au fait de supprimer le chemin ou pas *
    }

    public void setMyLocationEnable(boolean b) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestNeededPermissions();
        }
        else {
            googleMap.setMyLocationEnabled(b);
            if (b) zoomOnCurrentLocation();
        }
    }

    private int getCurrentSession() {
       return (int) State.getCurrentSession();
    }

    private void requestNeededPermissions() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // TODO: Rajouter Dialog ici
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_ACCESS_FINE_LOCATION);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}