package ie.dit.myswing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class TournamentMarkerSetup extends AppCompatActivity {

    private TextView accessCode, digitOne, digitTwo, digitThree, digitFour;
    private Button showKeyboard;
    private InputMethodManager keyboard;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference().child("courses");
    private DatabaseReference tournamentsRef = FirebaseDatabase.getInstance().getReference().child("tournaments");

    private String tournamentFirebaseKey, tournamentName, tournamentCourseName, tournamentCourseID,
            courseLatitude, courseLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tournament_marker_setup);

        getSupportActionBar().setTitle("Marker Setup");

        mAuth = FirebaseAuth.getInstance();

        tournamentFirebaseKey = getIntent().getStringExtra("tournamentFirebaseKey");
        tournamentName = getIntent().getStringExtra("tournamentName");
        tournamentCourseName = getIntent().getStringExtra("tournamentCourseName");
        tournamentCourseID = getIntent().getStringExtra("tournamentCourseID");
        coursesRef.child(tournamentCourseID).child("location").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                courseLatitude = dataSnapshot.child("latitude").getValue().toString();
                courseLongitude = dataSnapshot.child("longitude").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        digitOne = (TextView)findViewById(R.id.digit_one);
        digitTwo = (TextView)findViewById(R.id.digit_two);
        digitThree = (TextView)findViewById(R.id.digit_three);
        digitFour = (TextView)findViewById(R.id.digit_four);

        Random random = new Random();
        String id = String.format("%04d", random.nextInt(10000));
        accessCode = (TextView)findViewById(R.id.random_number);
        accessCode.setText(id);
        tournamentsRef.child(tournamentFirebaseKey).child("access codes").child(mAuth.getCurrentUser().getUid()).setValue(id);

        keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        showKeyboard = (Button)findViewById(R.id.show_keyboard);
        showKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (!digitOne.getText().equals("") && digitTwo.getText().equals("")
                    && digitThree.getText().equals("") && digitFour.getText().equals("")) {
                digitOne.setText("");
            }
            else if (!digitOne.getText().equals("") && !digitTwo.getText().equals("")
                        && digitThree.getText().equals("") && digitFour.getText().equals("")) {
                digitTwo.setText("");
            }
            else if (!digitOne.getText().equals("") && !digitTwo.getText().equals("")
                        && !digitThree.getText().equals("") && digitFour.getText().equals("")) {
                digitThree.setText("");
            }
            else if (!digitOne.getText().equals("") && !digitTwo.getText().equals("")
                        && !digitThree.getText().equals("") && !digitFour.getText().equals("")) {
                digitFour.setText("");
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_ENTER) {
            if (digitOne.getText().equals("") || digitTwo.getText().equals("") || digitThree.getText().equals("") || digitFour.getText().equals("")) {
                Toast.makeText(this, "Must Enter Valid Code", Toast.LENGTH_LONG).show();
            }
            else {
                final String code = digitOne.getText().toString() + digitTwo.getText() + digitThree.getText() + digitFour.getText();
                tournamentsRef.child(tournamentFirebaseKey).child("access codes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean match = false;
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            if (code.equals(data.getValue().toString())) {
                                match = true;
                                final String markerID = data.getKey();
                                usersRef.child(markerID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String markerName = dataSnapshot.child("first name").getValue().toString() + " " + dataSnapshot.child("last name").getValue().toString();
                                        String markerGender = dataSnapshot.child("tee box").getValue().toString();

                                        Intent confirmRoundIntent = new Intent(TournamentMarkerSetup.this, ConfirmRound.class);
                                        confirmRoundIntent.putExtra("tournamentFirebaseKey", tournamentFirebaseKey);
                                        confirmRoundIntent.putExtra("tournamentName", tournamentName);
                                        confirmRoundIntent.putExtra("tournamentCourseName", tournamentCourseName);
                                        confirmRoundIntent.putExtra("tournamentCourseID", tournamentCourseID);
                                        confirmRoundIntent.putExtra("tournamentCourseLatitude", courseLatitude);
                                        confirmRoundIntent.putExtra("tournamentCourseLongitude", courseLongitude);
                                        confirmRoundIntent.putExtra("tournamentMarkerID", markerID);
                                        confirmRoundIntent.putExtra("tournamentMarkerName", markerName);
                                        confirmRoundIntent.putExtra("tournamentMarkerGender", markerGender);

                                        keyboard.hideSoftInputFromWindow(findViewById(R.id.root).getWindowToken(), 0);

                                        startActivity(confirmRoundIntent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                                });
                                break;
                            }
                        }
                        if (!match) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(TournamentMarkerSetup.this);
                            builder.setTitle("Warning");
                            builder.setMessage("Entered code does not match any user in this tournament. Please try again.");
                            builder.setNegativeButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog warning = builder.create();
                            warning.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        }
        else {
            if (digitOne.getText().equals("")) {
                digitOne.setText(String.valueOf((char) event.getUnicodeChar()));
            }
            else if (!digitOne.getText().equals("") && digitTwo.getText().equals("")) {
                digitTwo.setText(String.valueOf((char) event.getUnicodeChar()));
            }
            else if (!digitOne.getText().equals("") && !digitTwo.getText().equals("")
                    && digitThree.getText().equals("")) {
                digitThree.setText(String.valueOf((char) event.getUnicodeChar()));
            }
            else if (!digitOne.getText().equals("") && !digitTwo.getText().equals("")
                    && !digitThree.getText().equals("") && digitFour.getText().equals("")) {
                digitFour.setText(String.valueOf((char) event.getUnicodeChar()));
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
