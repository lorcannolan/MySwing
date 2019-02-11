package ie.dit.myswing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConfigureCourse extends AppCompatActivity {

    private TabLayout tabLayout;
    private ConfigureCourseTabPageAdapter mPageAdapter;
    private ViewPager mViewPager;

    private String courseLatitude;
    private String courseLongitude;

    private static final String TAG = "ConfigureCourse";

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_course);

        // Originally, content was stored in frame layout. However, this was changed to view pager
        // to allow swiping between tabs. This also prevents map tab fragment from reloading.
        mViewPager = (ViewPager) findViewById(R.id.tab_container);

        mPageAdapter = new ConfigureCourseTabPageAdapter(getSupportFragmentManager());
        setupViewPager(mViewPager);

        tabLayout = (TabLayout)findViewById(R.id.tab_navigation);
        tabLayout.setupWithViewPager(mViewPager);

        Intent i = getIntent();
        String courseName = i.getStringExtra("courseName");
        getSupportActionBar().setTitle("Configure " + courseName);
    }

    public void setupViewPager (ViewPager viewPager) {
        ConfigureCourseTabPageAdapter adapter = new ConfigureCourseTabPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new ConfigureMapFragment(), "Map");
        adapter.addFragment(new ConfigureScorecardFragment(), "Scorecard");
        viewPager.setAdapter(adapter);
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
}
