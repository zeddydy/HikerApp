package com.hiker.app.fragments;
//package info.androidhive.materialtabs.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.hiker.app.activities.ConsultActivity;
import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.R;

public class HomeFragment extends Fragment {
    private MyStorageManager myStorageManager;
    private ListView listViewHistory;
    private TextView textViewNoElems;
    private SwipeRefreshLayout swipeContainer;

    public HomeFragment() {
        // Required empty public constructor
    }

    //TODO: Rendre la list Asynchrone
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Création de l'outil de gestion des données
        myStorageManager = new MyStorageManager(getActivity().getApplicationContext());

        //Récupération des différentes view
        listViewHistory = (ListView)view.findViewById(R.id.listViewHistory);
        textViewNoElems = (TextView)view.findViewById(R.id.textViewNoElems);

        //Remplissage de la listeview
        updateHistory();

        //Initialisation du swiê refresher
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateHistory();
                swipeContainer.setRefreshing(false);
            }
        });
        swipeContainer.setColorSchemeResources(R.color.colorPrimary);

        return view;
    }

    private void updateHistory() {
        Cursor cursor =  myStorageManager.getAllTracksDesc();

        if (cursor.getCount() > 0) textViewNoElems.setVisibility(View.GONE);
        else textViewNoElems.setVisibility(View.VISIBLE);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.item_home,
                cursor,
                new String[] {MyStorageManager.TRACKS_COLUMN_ID, MyStorageManager.TRACKS_COLUMN_NAME, MyStorageManager.TRACKS_COLUMN_DISTANCE},
                new int[] {R.id.textHomeID, R.id.textHomeTitle, R.id.textHomeDistanceValue},
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                //Conversion des mètres en kilomètres
                if (i == cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_DISTANCE)) {
                    ((TextView)view).setText(String.valueOf(cursor.getInt(i)/1000));
                    return true;
                } else if (i == cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_END_TIME)) {
                    //TODO
                }
                return false;
            }
        });

        listViewHistory.setAdapter(adapter);

        listViewHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ConsultActivity.class);
                intent.putExtra("id", Integer.valueOf(((TextView)view.findViewById(R.id.textHomeID)).getText().toString()));
                startActivity(intent);
            }
        });
    }
}