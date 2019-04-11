package ie.dit.myswing.rounds;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import ie.dit.myswing.R;
import ie.dit.myswing.map.TabPageAdapter;

public class RoundMapAndScorecard extends AppCompatActivity {

    private static final String TAG = "RoundMap&Scorecard";

    private TabLayout tabLayout;
    private TabPageAdapter mPageAdapter;
    private ViewPager mViewPager;

    private RoundMapFragment roundMapFragment;
    private RoundScorecardFragment roundScorecardFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_map_and_scorecard);

        mViewPager = (ViewPager) findViewById(R.id.play_tab_container);

        mPageAdapter = new TabPageAdapter(getSupportFragmentManager());
        setupViewPager(mViewPager);

        tabLayout = (TabLayout) findViewById(R.id.play_tab_navigation);
        tabLayout.setupWithViewPager(mViewPager);

        Intent i = getIntent();
        String courseName = i.getStringExtra("courseName");
        getSupportActionBar().setTitle(courseName);

    }

    public void setupViewPager (ViewPager viewPager) {
        TabPageAdapter adapter = new TabPageAdapter(getSupportFragmentManager());
        roundMapFragment = new RoundMapFragment();
        roundScorecardFragment = new RoundScorecardFragment();
        adapter.addFragment(roundMapFragment, "Map");
        adapter.addFragment(roundScorecardFragment, "Scorecard");
        viewPager.setAdapter(adapter);
    }

}
