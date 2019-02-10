package ie.dit.myswing;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ConfigureCourse extends AppCompatActivity {

    private TabLayout tabLayout;

    private TabLayout.OnTabSelectedListener mOnTabSelectedListener
            = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            Fragment selectedTab = null;
            switch (tab.getText().toString()) {
                case "Map":
                    selectedTab = new ConfigureMapFragment();
                    break;
                case "Scorecard":
                    selectedTab = new ConfigureScorecardFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.tab_container,
                    selectedTab).commit();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}

        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_course);

        Intent i = getIntent();
        String courseName = i.getStringExtra("courseName");

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Configure " + courseName);

        tabLayout = (TabLayout)findViewById(R.id.tab_navigation);
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.addTab(tabLayout.newTab().setText("Scorecard"));
        tabLayout.addOnTabSelectedListener(mOnTabSelectedListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.tab_container,
                new ConfigureMapFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            Intent backToHomeIntent = new Intent(ConfigureCourse.this, Home.class);
            startActivity(backToHomeIntent);
            finish();
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        tabLayout.removeOnTabSelectedListener(mOnTabSelectedListener);
    }
}
