package com.hiker.app.fragments;
//package info.androidhive.materialtabs.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hiker.app.activities.ConsultActivity;
import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.R;
import com.hiker.app.utils.Utils;

import java.io.ByteArrayInputStream;

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
                new String[] {MyStorageManager.TRACKS_COLUMN_ID, MyStorageManager.TRACKS_COLUMN_NAME, MyStorageManager.TRACKS_COLUMN_DISTANCE, MyStorageManager.TRACKS_COLUMN_END_TIME, MyStorageManager.TRACKS_COLUMN_IMAGE},
                new int[] {R.id.textHomeID, R.id.textHomeTitle, R.id.textHomeDistanceValue, R.id.textHomeTimeValue, R.id.homeImage},
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_DISTANCE)) {
                    //Conversion des mètres en kilomètres
                    ((TextView)view).setText(String.valueOf(cursor.getInt(i)/1000));
                    return true;
                } else if (i == cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_END_TIME)) {
                    //Toast.makeText(getActivity(), cursor.getLong(i) + " " + cursor.getLong(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_START_TIME)), Toast.LENGTH_SHORT).show();
                    ((TextView)view).setText(String.valueOf(Utils.dateToString(cursor.getLong(i) - cursor.getLong(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_START_TIME)))));
                    return true;
                } else if (i == cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_IMAGE)) {
                    byte [] blob = cursor.getBlob(i);
                    if (blob != null) { //TODO: Async Image Load
                        ByteArrayInputStream imageStream = new ByteArrayInputStream(blob);
                        ((ImageView) view).setImageBitmap(BitmapFactory.decodeStream(imageStream));
                    } else ((ViewGroup)view.getParent()).removeView(view);
                    return true;
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

        listViewHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final long id = Long.valueOf(((TextView)view.findViewById(R.id.textHomeID)).getText().toString());

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                myStorageManager.removeTrack(id);
                                updateHistory();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Voulez-vous vraiment supprimer la session \"" + ((TextView)view.findViewById(R.id.textHomeTitle)).getText().toString() + "\" de l'historique?").setPositiveButton("Oui", dialogClickListener).setNegativeButton("Non", dialogClickListener).show();

                return true;
            }
        });
    }
}