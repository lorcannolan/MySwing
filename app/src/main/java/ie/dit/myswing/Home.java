package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";

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
                    getSupportActionBar().setTitle("Select Round Type");
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
            getSupportActionBar().setTitle("Select Round Type");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
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
