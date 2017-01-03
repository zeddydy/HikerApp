package com.hiker.app.activities;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiker.app.utils.MyStorageManager;
import com.hiker.app.R;
import com.hiker.app.utils.Utils;

import java.io.ByteArrayInputStream;

public class ConsultActivity extends AppCompatActivity {
    private MyStorageManager myStorageManager;

    private Chronometer time;
    private TextView distance;
    private TextView speed;
    private TextView altitude;
    private TextView up;
    private TextView down;
    private TextView steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult);

        myStorageManager = new MyStorageManager(getApplicationContext());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Résumé");

        setup(getIntent().getIntExtra("id", -1));
    }


    private void setup(long id) {
        Cursor cursor = myStorageManager.getTrack(id);

        if(cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();

                ((TextView)findViewById(R.id.textConsultTitle)).setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_NAME)));

                byte [] blob = cursor.getBlob(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_IMAGE));
                if (blob != null) {
                    ByteArrayInputStream imageStream = new ByteArrayInputStream(blob);
                    ((ImageView) findViewById(R.id.imageConsult)).setImageBitmap(BitmapFactory.decodeStream(imageStream));
                }

                View v = findViewById(R.id.itemTime);
                ((TextView) v.findViewById(R.id.textTitle)).setText("Temps");
                time = ((Chronometer) v.findViewById(R.id.chronometer));
                time.setText(Utils.dateToString(cursor.getLong(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_END_TIME)) - cursor.getLong(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_START_TIME))));

                v = findViewById(R.id.itemDistance);
                ((TextView) v.findViewById(R.id.textTitle)).setText("Distance");
                ((TextView) v.findViewById(R.id.textUnit)).setText("m");
                distance = ((TextView) v.findViewById(R.id.textValue));
                distance.setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_DISTANCE))); //TODO: Gérer m / km

                v = findViewById(R.id.itemAltitude);
                ((TextView) v.findViewById(R.id.textTitle)).setText("Dénivelé");
                ((TextView) v.findViewById(R.id.textUnit)).setText("m");
                speed = ((TextView) v.findViewById(R.id.textValue));
                speed.setText("0"); //TODO

                v = findViewById(R.id.itemSpeed);
                ((TextView) v.findViewById(R.id.textTitle)).setText("Allure moyenne");
                ((TextView) v.findViewById(R.id.textUnit)).setText("km/h");
                altitude = ((TextView) v.findViewById(R.id.textValue));
                altitude.setText("0");  //TODO

                v = findViewById(R.id.itemUp);
                ((TextView) v.findViewById(R.id.textTitle)).setText("Distance montée");
                ((TextView) v.findViewById(R.id.textUnit)).setText("m");
                up = ((TextView) v.findViewById(R.id.textValue));
                up.setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_ASCEND)));

                v = findViewById(R.id.itemDown);
                ((TextView) v.findViewById(R.id.textTitle)).setText("Distance descendue");
                ((TextView) v.findViewById(R.id.textUnit)).setText("m");
                down = ((TextView) v.findViewById(R.id.textValue));
                down.setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_DESCEND)));

                v = findViewById(R.id.itemSteps);
                ((TextView) v.findViewById(R.id.textTitle)).setText("Pas");
                ((TextView) v.findViewById(R.id.textUnit)).setText("p");
                steps = ((TextView) v.findViewById(R.id.textValue));
                steps.setText(cursor.getString(cursor.getColumnIndex(MyStorageManager.TRACKS_COLUMN_STEPS)));
            }
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
