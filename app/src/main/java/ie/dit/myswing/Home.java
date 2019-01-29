package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    private TextView mTextMessage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private AppCompatButton signOut;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_play:
                    break;
                case R.id.navigation_rounds:
                    Intent roundsIntent = new Intent(Home.this, Rounds.class);
                    startActivity(roundsIntent);
                    break;
                case R.id.navigation_tournaments:
                    Intent tournamentsIntent = new Intent(Home.this, Tournaments.class);
                    startActivity(tournamentsIntent);
                    break;
                case R.id.navigation_map:
                    Intent mapIntent = new Intent(Home.this, Map.class);
                    startActivity(mapIntent);
                    break;
                case R.id.navigation_profile:
                    Intent profileIntent = new Intent(Home.this, Profile.class);
                    startActivity(profileIntent);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView title = (TextView)findViewById(R.id.playTitle);
        title.setText(R.string.title_play);

        BottomNavigationView navigation = (BottomNavigationView)findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem mItem = menu.getItem(0);
        mItem.setChecked(true);

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

        signOut = (AppCompatButton)findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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
