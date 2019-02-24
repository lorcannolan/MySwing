package ie.dit.myswing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
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
    private TextView selectedCourse;
    private AppCompatButton changeCourse;
    private FloatingTextButton confirmCourse;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_round);

        mAuth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        final String courseFirebaseKey = i.getStringExtra("courseFirebaseKey");
        String coursePlacesID = i.getStringExtra("coursePlacesID");
        final String courseName = i.getStringExtra("courseName");
        String courseLatitude = i.getStringExtra("courseLatitude");
        String courseLongitude = i.getStringExtra("courseLongitude");

        final Date date = Calendar.getInstance().getTime();
        final String mDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

        getSupportActionBar().setTitle("Confirm Round");

        enterHandicap = (EditText)findViewById(R.id.enter_handicap);
        selectedCourse = (TextView)findViewById(R.id.selected_course);
        selectedCourse.setText(courseName);

        changeCourse = (AppCompatButton)findViewById(R.id.change_course);
        changeCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goBackToPlay = new Intent(ConfirmRound.this, Home.class);
                startActivity(goBackToPlay);
                finish();
            }
        });

        confirmCourse = (FloatingTextButton)findViewById(R.id.confirm_round);
        confirmCourse.setOnClickListener(new View.OnClickListener() {
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
                            usersRef.child(mAuth.getUid()).child("rounds").child(roundID).child("date").setValue(mDate);
                            usersRef.child(mAuth.getUid()).child("rounds").child(roundID).child("handicap").setValue(enterHandicap.getText().toString());
                            usersRef.child(mAuth.getUid()).child("rounds").child(roundID).child("score").setValue(0);

                            Intent startPlayingIntent = new Intent(ConfirmRound.this, PlayMapAndScorecard.class);
                            startPlayingIntent.putExtra("courseName", courseName);
                            startPlayingIntent.putExtra("userGender", dataSnapshot.child("tee box").getValue().toString());
                            startPlayingIntent.putExtra("courseFirebaseKey", courseFirebaseKey);
                            startPlayingIntent.putExtra("roundID", roundID);
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
}
