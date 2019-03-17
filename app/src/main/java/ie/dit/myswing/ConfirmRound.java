package ie.dit.myswing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ConfirmRound extends AppCompatActivity {

    private EditText enterHandicap;
    private TextView selectedCourse, selectedTournament;
    private FloatingActionButton changeCourse, changeTournament;
    private FloatingTextButton confirmRound;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference().child("courses");

    private String tournamentFirebaseKey, tournamentName, tournamentCourseName, tournamentCourseID,
                    courseLatitude, courseLongitude, courseFirebaseKey, coursePlacesID, courseName,
                    todaysDateString;

    private Date todaysDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_round);

        mAuth = FirebaseAuth.getInstance();

        changeTournament = (FloatingActionButton) findViewById(R.id.edit_selected_tournament);
        changeCourse = (FloatingActionButton) findViewById(R.id.edit_selected_course);

        enterHandicap = (EditText)findViewById(R.id.enter_handicap);
        selectedCourse = (TextView)findViewById(R.id.selected_course);
        selectedTournament = (TextView)findViewById(R.id.selected_tournament);

        confirmRound = (FloatingTextButton)findViewById(R.id.confirm_round);

        getSupportActionBar().setTitle("Confirm Round");

        Intent i = getIntent();
        // If coming from select tournament
        if (i.hasExtra("tournamentFirebaseKey")) {
            tournamentFirebaseKey = i.getStringExtra("courseFirebaseKey");
            tournamentName = i.getStringExtra("tournamentName");
            tournamentCourseName = i.getStringExtra("tournamentCourseName");
            tournamentCourseID = i.getStringExtra("tournamentCourseID");
            coursesRef.child(tournamentCourseID).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    courseLatitude = dataSnapshot.child("latitude").getValue().toString();
                    courseLongitude = dataSnapshot.child("longitude").getValue().toString();
                    setupView();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });

            // If tournament round, don't show edit course button
            changeCourse.hide();
        }
        // If coming from select course
        else {
            courseFirebaseKey = i.getStringExtra("courseFirebaseKey");
            coursePlacesID = i.getStringExtra("coursePlacesID");
            courseName = i.getStringExtra("courseName");
            courseLatitude = i.getStringExtra("courseLatitude");
            courseLongitude = i.getStringExtra("courseLongitude");

            // Only allow functionality if personal round
            changeCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent selectCourseIntent = new Intent(ConfirmRound.this, PlaySelectCourse.class);
                    startActivity(selectCourseIntent);
                }
            });

            setupView();
        }

        todaysDate = Calendar.getInstance().getTime();
        todaysDateString = new SimpleDateFormat("dd/MM/yyyy").format(todaysDate);

        changeTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectTournamentIntent = new Intent(ConfirmRound.this, PlaySelectTournament.class);
                startActivity(selectTournamentIntent);
            }
        });

        confirmRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = v;
                if (enterHandicap.getText().toString().equals("")) {
                    Snackbar.make(v, "Must Enter Handicap", Snackbar.LENGTH_LONG).show();
                }
                else if (Integer.parseInt(enterHandicap.getText().toString()) < 0 || Integer.parseInt(enterHandicap.getText().toString()) > 36) {
                    Snackbar.make(v, "Invalid Handicap", Snackbar.LENGTH_LONG).show();
                }
                else {
                    usersRef.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String roundID = usersRef.child(mAuth.getUid()).child("rounds").push().getKey();
                            usersRef.child(mAuth.getUid()).child("rounds").child(roundID).child("courseID").setValue(courseName);
                            usersRef.child(mAuth.getUid()).child("rounds").child(roundID).child("date").setValue(todaysDateString);
                            usersRef.child(mAuth.getUid()).child("rounds").child(roundID).child("handicap").setValue(enterHandicap.getText().toString());
                            usersRef.child(mAuth.getUid()).child("rounds").child(roundID).child("score").setValue(0);

                            Intent startPlayingIntent = new Intent(ConfirmRound.this, PlayMapAndScorecard.class);
                            startPlayingIntent.putExtra("courseName", courseName);
                            startPlayingIntent.putExtra("userGender", dataSnapshot.child("tee box").getValue().toString());
                            startPlayingIntent.putExtra("courseFirebaseKey", courseFirebaseKey);
                            startPlayingIntent.putExtra("roundID", roundID);
                            startPlayingIntent.putExtra("courseLatitude", courseLatitude);
                            startPlayingIntent.putExtra("courseLongitude", courseLongitude);
                            startActivity(startPlayingIntent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }
        });
    }

    public void setupView() {
        if (getIntent().hasExtra("tournamentFirebaseKey")) {
            selectedCourse.setText(tournamentCourseName);
            selectedTournament.setText(tournamentName);
        }
        else {
            selectedCourse.setText(courseName);
            selectedTournament.setText("N/A");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Intent homeActivity = new Intent(ConfirmRound.this, Home.class);
            startActivity(homeActivity);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cancel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.done) {
            Intent backToHomeIntent = new Intent(ConfirmRound.this, Home.class);
            startActivity(backToHomeIntent);
            finish();
        }
        return true;
    }
}
