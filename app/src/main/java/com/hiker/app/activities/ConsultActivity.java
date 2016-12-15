package com.hiker.app.activities;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.R;

public class ConsultActivity extends AppCompatActivity {
    private MyStorageManager myStorageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult);

        myStorageManager = new MyStorageManager(getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        int id = getIntent().getIntExtra("id", -1);

        if (id != -1) {
            ListView lv = (ListView)findViewById(R.id.listViewData);
            Cursor cursor =  myStorageManager.getPointsOfTrack(id);
            //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, values);
            ListAdapter adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2,
                    cursor,
                    new String[] {MyStorageManager.POINTS_COLUMN_LATITUDE, MyStorageManager.POINTS_COLUMN_LONGITUDE},
                    new int[] {android.R.id.text1, android.R.id.text2},
                    0);

            lv.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
