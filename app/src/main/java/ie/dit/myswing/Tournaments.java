package ie.dit.myswing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Tournaments extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_play:
                    Intent playIntent = new Intent(Tournaments.this, Home.class);
                    startActivity(playIntent);
                    break;
                case R.id.navigation_rounds:
                    Intent roundsIntent = new Intent(Tournaments.this, Rounds.class);
                    startActivity(roundsIntent);
                    break;
                case R.id.navigation_tournaments:
                    break;
                case R.id.navigation_map:
                    Intent mapIntent = new Intent(Tournaments.this, Map.class);
                    startActivity(mapIntent);
                    break;
                case R.id.navigation_profile:
                    Intent profileIntent = new Intent(Tournaments.this, Profile.class);
                    startActivity(profileIntent);
                    break;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournaments);

        BottomNavigationView navigation = (BottomNavigationView)findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navigation.getMenu();
        MenuItem mItem = menu.getItem(2);
        mItem.setChecked(true);

        TextView title = (TextView)findViewById(R.id.tournamentsTitle);
        title.setText(R.string.title_tournaments);
    }
}
