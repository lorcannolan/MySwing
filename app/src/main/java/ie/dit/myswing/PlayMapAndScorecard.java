package ie.dit.myswing;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Set;
import java.util.UUID;

public class PlayMapAndScorecard extends AppCompatActivity {

    private static final String TAG = "PlayMap&Scorecard";

    private TabLayout tabLayout;
    private TabPageAdapter mPageAdapter;
    private ViewPager mViewPager;

    private PlayMapFragment playMapFragment;
    private PlayScorecardFragment playScorecardFragment;

    private String bluetoothDeviceAddress;
    /*
        UUID taken from below source:
            -> https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createInsecureRfcommSocketToServiceRecord(java.util.UUID)
        UUID is a well-known SPP UUID for Bluetooth serial boards.
    */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice bluetoothDevice;

    BluetoothConnectionService bluetoothConnectionService;

    private static final long UPDATE_INTERVAL = 3000;
    private static final long FASTEST_INTERVAL = 1500;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private FusedLocationProviderClient fusedLocationProviderClient2;

    private Location lastKnownLocation, shotLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_map_and_scorecard);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient2 = LocationServices.getFusedLocationProviderClient(this);

        mViewPager = (ViewPager) findViewById(R.id.play_tab_container);

        mPageAdapter = new TabPageAdapter(getSupportFragmentManager());
        setupViewPager(mViewPager);

        tabLayout = (TabLayout) findViewById(R.id.play_tab_navigation);
        tabLayout.setupWithViewPager(mViewPager);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bluetoothDeviceAddress = prefs.getString("BT Device Address", "");
        Log.d(TAG, "***************\nBT Device Address: " + bluetoothDeviceAddress);

        Intent i = getIntent();
        String courseName = i.getStringExtra("courseName");
        getSupportActionBar().setTitle("Playing " + courseName);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices and find device that was paired in PlayFragment
            for (BluetoothDevice device : pairedDevices) {
                if (!bluetoothDeviceAddress.equals("")) {
                    if (bluetoothDeviceAddress.equals(device.getAddress())) {
                        Log.d(TAG, "***************\nDevice Name: " + device.getName() + "\nDevice Address: " + device.getAddress() + "\n");
                        bluetoothDevice = device;
                        bluetoothConnectionService = new BluetoothConnectionService(this, PlayMapAndScorecard.this);
                        startBTConnection(bluetoothDevice, MY_UUID);
                    }
                }
            }
        }

        getLocation();

    }

    // Method constantly runs to receive location updates
    public void getLocation() {
        // LocationRequest is used to constantly retrieve user's location at an interval specified
        LocationRequest locationRequestHighAccuracy = new LocationRequest();
        locationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        locationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        // LocationCallback method gets called every 3 seconds
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient2.requestLocationUpdates(locationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();

                        if (location != null) {
                            lastKnownLocation = location;

                            playMapFragment.setLastKnownLocation(lastKnownLocation);

                            if (shotLocation != null) {
                                float[] results = new float[3];
                                Location.distanceBetween(shotLocation.getLatitude(), shotLocation.getLongitude(),
                                        lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(),
                                        results);
                                Toast.makeText(PlayMapAndScorecard.this, "" + results[0], Toast.LENGTH_LONG).show();
                                if (results[0] > 20) {
                                    // add shot once the player has walked 25 metres away from their ball
                                    playMapFragment.prepareAddShot(new LatLng(
                                            shotLocation.getLatitude(),
                                            shotLocation.getLongitude()
                                    ));
                                    Toast.makeText(PlayMapAndScorecard.this, "Shot added", Toast.LENGTH_LONG).show();
                                    shotLocation = null;
                                }
                            }

                            Log.d(TAG, "***************\nCalling checkCurrentHole");
                            playMapFragment.checkCurrentHole(lastKnownLocation);

                        }
                    }
                },
                Looper.myLooper());
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public void error() {
        Toast.makeText(this, "Stream input error", Toast.LENGTH_LONG).show();
    }

    public void addShot() {
        Log.d(TAG, "***********\nAdd shot called");

        // get last known location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    shotLocation = task.getResult();
                }
            }
        });
    }

    /*
        Start receiving data input from Arduino.
    */
    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "***************\nstartBTConnection: Initializing RFCOM Bluetooth Connection.");

        bluetoothConnectionService.startClient(device, uuid);
    }

    public void setupViewPager (ViewPager viewPager) {
        TabPageAdapter adapter = new TabPageAdapter(getSupportFragmentManager());
        playMapFragment = new PlayMapFragment();
        playScorecardFragment = new PlayScorecardFragment();
        adapter.addFragment(playMapFragment, "Map");
        adapter.addFragment(playScorecardFragment, "Scorecard");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.finish_round, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.finish) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayMapAndScorecard.this);
            builder.setTitle("Are You Sure You're Finished?");
            builder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent backToHomeIntent = new Intent(PlayMapAndScorecard.this, Home.class);
                            startActivity(backToHomeIntent);
                            finish();
                        }
                    });
            builder.setNegativeButton("No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog warning = builder.create();
            warning.show();
        }
        return true;
    }

}
