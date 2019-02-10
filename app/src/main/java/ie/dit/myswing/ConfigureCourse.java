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

    private OnLatLangReceivedListener mLatLngListener;

    private static final String TAG = "ConfigureCourse";

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_course);

        setTitleAndLatLng();

        // Originally, content was stored in frame layout. However, this was changed to view pager
        // to allow swiping between tabs. This also prevents map tab fragment from reloading.
        mViewPager = (ViewPager) findViewById(R.id.tab_container);

        mPageAdapter = new ConfigureCourseTabPageAdapter(getSupportFragmentManager());
        setupViewPager(mViewPager);

        tabLayout = (TabLayout)findViewById(R.id.tab_navigation);
        tabLayout.setupWithViewPager(mViewPager);
    }

    public void setTitleAndLatLng() {
        Intent i = getIntent();
        final String coursePlacesID = i.getStringExtra("coursePlacesID");

        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    // If data exists in database, break and set boolean
                    if (data.child("placesID").getValue().toString().equals(coursePlacesID)) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        getSupportActionBar().setTitle("Configure " + data.child("name").getValue().toString());
                        setCourseLatitude(data.child("location").child("latitude").getValue().toString());
                        setCourseLongitude(data.child("location").child("longitude").getValue().toString());
                        break;
                    }
                }
                mLatLngListener.onLatLngReceived(getCourseLatitude(), getCourseLongitude());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public interface OnLatLangReceivedListener {
        void onLatLngReceived(String latitude, String longitude);
    }

    public void setLatLngListener(OnLatLangReceivedListener listener) {
        this.mLatLngListener = listener;
    }

    public void setCourseLatitude(String courseLatitude) {
        this.courseLatitude = courseLatitude;
    }

    public void setCourseLongitude(String courseLongitude) {
        this.courseLongitude = courseLongitude;
    }

    public String getCourseLatitude() {
        return courseLatitude;
    }

    public String getCourseLongitude() {
        return courseLongitude;
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
