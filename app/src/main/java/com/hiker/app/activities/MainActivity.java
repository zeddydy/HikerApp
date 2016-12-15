package com.hiker.app.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.hiker.app.fragments.CompassFragment;
import com.hiker.app.utils.Constants;
import com.hiker.app.fragments.HomeFragment;
import com.hiker.app.fragments.MapFragment;
import com.hiker.app.utils.MyBroadcastReceiver;
import com.hiker.app.R;
import com.hiker.app.fragments.SessionFragment;
import com.hiker.app.utils.State;
import com.hiker.app.services.TrackerService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fabLocation;

    private HomeFragment homeFragment;
    private SessionFragment sessionFragment;
    private MapFragment mapFragment;
    private CompassFragment compassFragment;

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private int[] tabIcons = {
            R.drawable.ic_tab_home,
            R.drawable.ic_tab_session,
            R.drawable.ic_tab_map,
            R.drawable.ic_tab_compass
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUpActivity();
    }

    private void setupUpActivity() {
        //Initisalisation de la Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Accueil");
        setSupportActionBar(toolbar);

        //Initialisation du viewPager (différents onglets)
        viewPager = (ViewPager)findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {    //TODO: Gestions des Resume / Pause activies
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(((ViewPagerAdapter)viewPager.getAdapter()).getPageTitleCustom(position));
                updateTabIcons(position);

                switch (position) {
                    case 2:
                        fabLocation.show();
                        break;
                    default:
                        fabLocation.hide();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //Initialisation de la bar des onglets
        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        //Initialisation du bouton flottant de session
        FloatingActionButton fabSession = (FloatingActionButton) findViewById(R.id.fabSession);
        if (State.isServiceTrackerRuning()) fabSession.setImageResource(R.drawable.ic_float_plus);
        fabSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Démarrage / Arrêt du service Tracker
                if (!State.isServiceTrackerRuning()) {
                    //Ajout d'une dialogue box pour crée la session ici
                    startService(new Intent(getBaseContext(), TrackerService.class));
                    ((FloatingActionButton)view).setImageResource(R.drawable.quantum_ic_stop_white_24);
                    Snackbar.make(view, "Démarrage d'une session...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                else {
                    long idTmp = State.getCurrentSession();
                    stopService(new Intent(getBaseContext(), TrackerService.class));
                    mapFragment.saveSnapshot(idTmp);

                    ((FloatingActionButton)view).setImageResource(R.drawable.ic_float_plus);
                    Snackbar.make(view, "Arrêt de la session en cours...", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        //Initialisation du bouton flottant de localisation
        fabLocation = (FloatingActionButton) findViewById(R.id.fabLocation);
        fabLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapFragment.zoomOnCurrentLocation();
            }
        });
        fabLocation.hide();

        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_BROADCAST);
        MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastReceiver, intentFilter);
    }

    private void setupTabIcons() {
        for(int i = 0; i < tabIcons.length; i++) {
            tabLayout.getTabAt(i).setIcon(tabIcons[i]);
            if (i != 0) tabLayout.getTabAt(i).getIcon().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
        }
    }

    private void updateTabIcons(int position) {
        for(int i = 0; i < tabIcons.length; i++) {
            if (i != position) tabLayout.getTabAt(i).getIcon().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
            else tabLayout.getTabAt(i).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        homeFragment = new HomeFragment();
        sessionFragment = new SessionFragment();
        mapFragment = new MapFragment();
        compassFragment = new CompassFragment();

        adapter.addFragment(homeFragment, "Accueil");
        adapter.addFragment(sessionFragment, "Session");
        adapter.addFragment(mapFragment, "Carte");
        adapter.addFragment(compassFragment, "Boussole");

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    public void onSessionStart() {
        sessionFragment.startChronometer();
    }

    public void onSessionStop() {
        updateFragments();
        updateMap();
        sessionFragment.stopChronometer();
    }

    public void updateFragments() {
        sessionFragment.update();
    }

    public void updateMap() {
        mapFragment.updatePath();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_units, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_units) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return mFragmentTitleList.get(position);
            return null;
        }

        public CharSequence getPageTitleCustom(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.setMyLocationEnable(true);
                } else {
                    // Permission Denied
                    Toast.makeText(this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
