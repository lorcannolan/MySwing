package ie.dit.myswing;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SelectedRound extends AppCompatActivity {

    private String roundKey, courseName, roundDate, courseID;
    private int totalPutts, handicap, score, toPar, netScore, totalPoints, longestDrive;
    private float avgPutts;

    private TextView grossTextView, netTextView, pointsTextView, handicapTextView, longestDriveTextView, averagePuttsTextView, courseTextView, dateTextView;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DatabaseReference roundsRef, courseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_round);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle("Rounds");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));

        roundKey = getIntent().getStringExtra("roundFirebaseKey");
        courseName = getIntent().getStringExtra("courseName");
        roundDate = getIntent().getStringExtra("roundDate");
        courseID = getIntent().getStringExtra("courseID");
        totalPutts = getIntent().getIntExtra("totalPutts", 0);
        handicap = getIntent().getIntExtra("handicap", 0);
        score = getIntent().getIntExtra("score", 0);
        toPar = getIntent().getIntExtra("toPar", 0);

        roundsRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("rounds").child(roundKey);
        roundsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                netScore = 0;
                totalPoints = 0;
                longestDrive = 0;
                avgPutts = Float.parseFloat(dataSnapshot.child("total putts").getValue().toString()) / (float)dataSnapshot.child("holes").getChildrenCount();
                for (DataSnapshot data : dataSnapshot.child("holes").getChildren()) {
                    netScore += Integer.parseInt(data.child("net score").getValue().toString());
                    totalPoints += Integer.parseInt(data.child("points").getValue().toString());
                    if (Integer.parseInt(data.child("drive distance").getValue().toString()) > longestDrive) {
                        longestDrive = Integer.parseInt(data.child("drive distance").getValue().toString());
                    }
                }
                setValues();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        courseRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseID);

        grossTextView = (TextView) findViewById(R.id.gross_score);
        netTextView = (TextView) findViewById(R.id.net_score);
        pointsTextView = (TextView) findViewById(R.id.stableford_score);
        handicapTextView = (TextView) findViewById(R.id.round_handicap);
        longestDriveTextView = (TextView) findViewById(R.id.longest_drive);
        averagePuttsTextView = (TextView) findViewById(R.id.avg_putting);
        courseTextView = (TextView) findViewById(R.id.selected_round_course);
        dateTextView = (TextView) findViewById(R.id.selected_round_date);

    }

    public void setValues() {
        grossTextView.setText(score + "");
        netTextView.setText(netScore + "");
        pointsTextView.setText(totalPoints + "");
        handicapTextView.setText(handicap + "");
        longestDriveTextView.setText(longestDrive + "m");
        averagePuttsTextView.setText(String.format("%.2f", avgPutts));
        courseTextView.setText(courseName);
        dateTextView.setText(roundDate);
    }
}
