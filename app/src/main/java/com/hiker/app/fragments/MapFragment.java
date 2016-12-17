package com.hiker.app.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hiker.app.utils.Constants;
import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.R;
import com.hiker.app.utils.State;

import java.io.ByteArrayOutputStream;

public class MapFragment extends Fragment {
    private final Object syncToken = new Object();

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
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        //Zoom to current Location
                        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(16).build();
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
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
        } else clearPath();
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

    public void saveSnapshot(long id) {
        final long i = id;
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    snapshot.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] img = stream.toByteArray();

                    myStorageManager.updateTrackImage(i, img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        if (myStorageManager != null) {
            Cursor cursor = myStorageManager.getPointsOfTrack(i);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                while (!cursor.isAfterLast()) {
                    builder.include(new LatLng(cursor.getFloat(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_LATITUDE)), cursor.getFloat(cursor.getColumnIndex(MyStorageManager.POINTS_COLUMN_LONGITUDE))));
                    cursor.moveToNext();
                }
                cursor.close();

                LatLngBounds bounds = builder.build();

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 1400, 800, 50);   //TODO Peut-être le faire dans une map dédiée?

                googleMap.moveCamera(cu);
            }
        } else zoomOnCurrentLocation();

        googleMap.snapshot(callback);

        //setMyLocationEnable(true);
        zoomOnCurrentLocation();    //TODO Rendre le déplacement instanné?
    }

    private int getCurrentSession() {
       return (int) State.getCurrentSession();
    } //TODO: Remove?

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