package ie.dit.myswing.greeting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ie.dit.myswing.map.MapFragment;
import ie.dit.myswing.play.PlayFragment;
import ie.dit.myswing.profile.ProfileFragment;
import ie.dit.myswing.R;
import ie.dit.myswing.rounds.RoundsFragment;
import ie.dit.myswing.tournaments.TournamentsFragment;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;

    // Tutorial on BottomNavigationView followed at this source:
    //      - https://www.youtube.com/watch?v=tPV8xA7m-iw
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_play:
                    selectedFragment = new PlayFragment();
                    getSupportActionBar().setTitle("Connect Bluetooth Device");
                    break;
                case R.id.navigation_rounds:
                    selectedFragment = new RoundsFragment();
                    getSupportActionBar().setTitle("Rounds");
                    break;
                case R.id.navigation_tournaments:
                    selectedFragment = new TournamentsFragment();
                    getSupportActionBar().setTitle("Tournaments");
                    break;
                case R.id.navigation_map:
                    selectedFragment = new MapFragment();
                    getSupportActionBar().setTitle("Choose Course to Configure");
                    break;
                case R.id.navigation_profile:
                    selectedFragment = new ProfileFragment();
                    getSupportActionBar().setTitle("Profile");
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
        }
    };

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private BottomNavigationView navigation;

    private boolean locationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        navigation = (BottomNavigationView)findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));

        Intent i = getIntent();
        String moveToFragment = i.getStringExtra("fragment");
        if (moveToFragment == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PlayFragment()).commit();
            getSupportActionBar().setTitle("Connect Bluetooth Device");
        }
        else if (moveToFragment.contentEquals("Play")) {
            navigation.setSelectedItemId(R.id.navigation_play);
        }
        // Moves to map fragment directly after course was added
        else if (moveToFragment.contentEquals("Map")){
            navigation.setSelectedItemId(R.id.navigation_map);
        }
        // Moves to profile fragment directly after club/society was joined
        else if (moveToFragment.contentEquals("Profile")){
            navigation.setSelectedItemId(R.id.navigation_profile);
        }
        // Moves to tournament fragment directly after creating tournament
        else if (moveToFragment.contentEquals("Tournaments")){
            navigation.setSelectedItemId(R.id.navigation_tournaments);
        }

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(Home.this, "Signing out.", Toast.LENGTH_SHORT).show();
                    Intent loginActivityIntent = new Intent(Home.this, Login.class);
                    startActivity(loginActivityIntent);
                }
            }
        };
    }

    private boolean checkMapServices() {
        if (isServicesIntalled()) {
            if (isMapsEnabled()) {
                return true;
            }
        }
        return false;
    }

    // Method ensures Google Play Services is installed so user can access location requests
    public boolean isServicesIntalled() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(Home.this);

        if (available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesInstalled: Google Play Services is working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesIntalled: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(Home.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // Is GPS enabled on the device
    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    // Prompts user with a dialog to enable location services
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        // onActivityResult method is called after user has selected whether or not to enable locations
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (!locationPermissionGranted) {
                    getLocationPermission();
                }
            }
        }
    }

    private void getLocationPermission() {
        /*
            Request location permission, so that we can get the location of the
            device. The result of the permission request is handled by a callback,
            onRequestPermissionsResult.
        */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }
        else {
            // Dialog to prompt user to access location permission
            // onRequestPermissionsResult called after user selection
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (!locationPermissionGranted) {
                getLocationPermission();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);

        MenuItem item = menu.getItem(0);
        SpannableString s = new SpannableString(menu.getItem(0).toString());
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
        item.setTitle(s);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            mAuth.signOut();
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            // If not removed, this listener will be active at all times
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }



}
