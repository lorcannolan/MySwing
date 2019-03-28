package ie.dit.myswing;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Set;
import java.util.UUID;

public class PlayMapAndScorecard extends AppCompatActivity {

    private static final String TAG = "PlayMap&Scorecard";

    private TabLayout tabLayout;
    private TabPageAdapter mPageAdapter;
    private ViewPager mViewPager;

    private String bluetoothDeviceAddress;
    /*
        UUID taken from below source:
            -> https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createInsecureRfcommSocketToServiceRecord(java.util.UUID)
        UUID is a well-known SPP UUID for Bluetooth serial boards.
    */
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice bluetoothDevice;

    BluetoothConnectionService bluetoothConnectionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_map_and_scorecard);

        mViewPager = (ViewPager) findViewById(R.id.play_tab_container);

        mPageAdapter = new TabPageAdapter(getSupportFragmentManager());
        setupViewPager(mViewPager);

        tabLayout = (TabLayout)findViewById(R.id.play_tab_navigation);
        tabLayout.setupWithViewPager(mViewPager);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bluetoothDeviceAddress = prefs.getString("BT Device Address", "");
        Log.d(TAG, "***************\nBT Device Address: " + bluetoothDeviceAddress);

        Intent i = getIntent();
        String courseName = i.getStringExtra("courseName");
        getSupportActionBar().setTitle("Playing " + courseName);

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
                        bluetoothConnectionService = new BluetoothConnectionService(this);
                        startBTConnection(bluetoothDevice, MY_UUID);
                    }
                }
            }
        }

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
        adapter.addFragment(new PlayMapFragment(), "Map");
        adapter.addFragment(new PlayScorecardFragment(), "Scorecard");
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
