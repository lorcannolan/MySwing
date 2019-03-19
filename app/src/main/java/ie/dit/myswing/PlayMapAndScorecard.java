package ie.dit.myswing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PlayMapAndScorecard extends AppCompatActivity {

    private TabLayout tabLayout;
    private TabPageAdapter mPageAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_map_and_scorecard);

        mViewPager = (ViewPager) findViewById(R.id.play_tab_container);

        mPageAdapter = new TabPageAdapter(getSupportFragmentManager());
        setupViewPager(mViewPager);

        tabLayout = (TabLayout)findViewById(R.id.play_tab_navigation);
        tabLayout.setupWithViewPager(mViewPager);

        Intent i = getIntent();
        String courseName = i.getStringExtra("courseName");
        getSupportActionBar().setTitle("Playing " + courseName);

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
