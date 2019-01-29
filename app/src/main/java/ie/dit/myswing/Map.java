package ie.dit.myswing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Map extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_play:
                    Intent playIntent = new Intent(Map.this, Home.class);
                    startActivity(playIntent);
                    break;
                case R.id.navigation_rounds:
                    Intent roundsIntent = new Intent(Map.this, Rounds.class);
                    startActivity(roundsIntent);
                    break;
                case R.id.navigation_tournaments:
                    Intent tournamentsIntent = new Intent(Map.this, Tournaments.class);
                    startActivity(tournamentsIntent);
                    break;
                case R.id.navigation_map:
                    break;
                case R.id.navigation_profile:
                    Intent profileIntent = new Intent(Map.this, Profile.class);
                    startActivity(profileIntent);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        BottomNavigationView navigation = (BottomNavigationView)findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem mItem = menu.getItem(3);
        mItem.setChecked(true);

        TextView title = (TextView)findViewById(R.id.mapTitle);
        title.setText(R.string.title_map);
    }
}
