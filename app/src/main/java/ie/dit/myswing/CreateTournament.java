package ie.dit.myswing;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class CreateTournament extends AppCompatActivity {

    private FloatingTextButton confirm;
    private EditText editTextTournamentName, editTextTournamentType;
    private FloatingActionButton selectCourseFAB, selectDateFAB;
    private TextView textViewCourseName, textViewDate;
    private String courseName, courseFirebaseKey;

    DatabaseReference userRef, tournamentsRef, coursesRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tournament);

        getSupportActionBar().setTitle("Create Tournament");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        tournamentsRef = FirebaseDatabase.getInstance().getReference().child("tournaments");
        coursesRef = FirebaseDatabase.getInstance().getReference().child("courses");

        editTextTournamentName = (EditText) findViewById(R.id.tournament_name);

        textViewCourseName = (TextView) findViewById(R.id.selected_course);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (getIntent().hasExtra("tournamentFragment") || getIntent().hasExtra("playFragment")) {
            editor.putString("Course Name", "");
            editor.putString("Course Firebase Key", "");
            editor.apply();
        }
        courseName = prefs.getString("Course Name", "no course");
        courseFirebaseKey = prefs.getString("Course Firebase Key", "");
        if (!courseName.equalsIgnoreCase("no course")) {
            textViewCourseName.setText(courseName);
        }

        selectCourseFAB = (FloatingActionButton) findViewById(R.id.select_course_fab);
        selectCourseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectCourseIntent = new Intent(CreateTournament.this, JoinClub.class);
                selectCourseIntent.putExtra("source", "Create Tournament Select Club");
                startActivity(selectCourseIntent);
            }
        });

        textViewDate = (TextView) findViewById(R.id.selected_date);

        selectDateFAB = (FloatingActionButton) findViewById(R.id.select_date_fab);
        selectDateFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                /*
                Date Picker Dialog Constructor Parameters:
                    - context
                    - OnDateSet listener
                    - year, month and day default values obtained above
                */
                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateTournament.this, R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // When OK is selected and date is set, TextView field is populated with the selected values
                                textViewDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        editTextTournamentType = (EditText) findViewById(R.id.tournament_type);
        editTextTournamentType.setInputType(InputType.TYPE_NULL);
        editTextTournamentType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateTournament.this);
                builder.setTitle("Select the Tournament Type");
                builder.setItems(R.array.tournament_types, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Resources res = CreateTournament.this.getResources();
                        String[] types = res.getStringArray(R.array.tournament_types);
                        editTextTournamentType.setText(types[which]);
                        if (types[which].equalsIgnoreCase("casual")) {
                            confirm.setTitle("Invite Players");
                        }
                        else {
                            if (confirm.getTitle().toString().equalsIgnoreCase("invite players")) {
                                confirm.setTitle("Confirm Tournament");
                            }
                        }
                    }
                });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog chooseTeeBoxes = builder.create();
                chooseTeeBoxes.show();
            }
        });

        confirm = (FloatingTextButton) findViewById(R.id.confirm_tournament);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (editTextTournamentType.getText().toString().equals("")) {
                    Snackbar.make(v, "Must Select Tournament Type", Snackbar.LENGTH_LONG).show();
                }
                else {
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            if (confirm.getTitle().equalsIgnoreCase("confirm tournament")) {
                                // If user has selected to create club event but is not a member of a club
                                if (editTextTournamentType.getText().toString().equalsIgnoreCase("club")
                                        && !dataSnapshot.hasChild("club")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateTournament.this);
                                    builder.setTitle("Error");
                                    builder.setMessage("You need to be a member of a club to create a club Tournament. Would you like to join a Club?");
                                    builder.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent selectCourseIntent = new Intent(CreateTournament.this, JoinClub.class);
                                                    selectCourseIntent.putExtra("source", "Create Tournament Join Club");
                                                    startActivity(selectCourseIntent);
                                                }
                                            });
                                    builder.setNegativeButton("No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog chooseTeeBoxes = builder.create();
                                    chooseTeeBoxes.show();
                                }
                                // If user has selected to create society event but is not a member of a society
                                else if (editTextTournamentType.getText().toString().equalsIgnoreCase("society")
                                        && !dataSnapshot.hasChild("society")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(CreateTournament.this);
                                    builder.setTitle("Error");
                                    builder.setMessage("You need to be a member of a society to create a society Tournament. Would you like to join a Society?");
                                    builder.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent selectSocietyIntent = new Intent(CreateTournament.this, JoinSociety.class);
                                                    selectSocietyIntent.putExtra("source", "Create Tournament");
                                                    startActivity(selectSocietyIntent);
                                                }
                                            });
                                    builder.setNegativeButton("No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });

                                    AlertDialog chooseTeeBoxes = builder.create();
                                    chooseTeeBoxes.show();
                                }
                                // If user has selected to create society/club event and is a member of a society/club
                                else if ((editTextTournamentType.getText().toString().equalsIgnoreCase("club") && dataSnapshot.hasChild("club"))
                                        || (editTextTournamentType.getText().toString().equalsIgnoreCase("society") && dataSnapshot.hasChild("society"))) {
                                    // ensure other tournament fields are not empty
                                    if (editTextTournamentName.getText().toString().equals("")
                                            || textViewCourseName.getText().toString().equals("")
                                            || textViewDate.getText().toString().equals("")) {
                                        Snackbar.make(v, "Must Enter All Details", Snackbar.LENGTH_LONG).show();
                                    }
                                    // Create tournament and invite all members of club/society
                                    else {
                                        // Date selected should be today's date or sometime in the future
                                        try {
                                            Date selectedDate = new SimpleDateFormat("dd/MM/yyyy").parse(textViewDate.getText().toString());
                                            Calendar today = Calendar.getInstance();
                                            today.set(android.icu.util.Calendar.HOUR_OF_DAY, 0);
                                            today.set(android.icu.util.Calendar.MINUTE, 0);
                                            today.set(android.icu.util.Calendar.SECOND, 0);
                                            today.set(android.icu.util.Calendar.MILLISECOND, 0);
                                            if (selectedDate.before(today.getTime())) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateTournament.this);
                                                builder.setTitle("Warning");
                                                builder.setMessage("Cannot Select Date Before Today's Date");
                                                builder.setNegativeButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });

                                                AlertDialog chooseTeeBoxes = builder.create();
                                                chooseTeeBoxes.show();
                                            }
                                            else {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateTournament.this);
                                                builder.setTitle("Confirm Tournament Details");
                                                builder.setPositiveButton("Confirm",
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                String id = tournamentsRef.push().getKey();
                                                                tournamentsRef.child(id).child("name").setValue(editTextTournamentName.getText().toString());
                                                                tournamentsRef.child(id).child("course").setValue(courseFirebaseKey);
                                                                tournamentsRef.child(id).child("date").setValue(textViewDate.getText().toString());
                                                                if (editTextTournamentType.getText().toString().equalsIgnoreCase("club")) {
                                                                    tournamentsRef.child(id).child("club").setValue(dataSnapshot.child("club").getValue());
                                                                }
                                                                else {
                                                                    tournamentsRef.child(id).child("society").setValue(dataSnapshot.child("society").getValue());
                                                                }

                                                                // If creating tournament while starting play
                                                                if (getIntent().hasExtra("playFragment")) {
                                                                    Intent returnToPlaySetup = new Intent(CreateTournament.this, PlaySelectTournament.class);
                                                                    startActivity(returnToPlaySetup);
                                                                    finish();
                                                                }
                                                                // If creating tournament from within tournaments
                                                                else {
                                                                    Intent returnToTournamentsIntent = new Intent(CreateTournament.this, Home.class);
                                                                    returnToTournamentsIntent.putExtra("fragment", "Tournaments");
                                                                    startActivity(returnToTournamentsIntent);
                                                                    finish();
                                                                }
                                                            }
                                                        });
                                                builder.setNegativeButton("Cancel",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });

                                                AlertDialog chooseTeeBoxes = builder.create();
                                                chooseTeeBoxes.show();
                                            }
                                        }
                                        catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            // If tournament type casual is selected
                            else {
                                // ensure other tournament fields are not empty
                                if (editTextTournamentName.getText().toString().equals("")
                                        || textViewCourseName.getText().toString().equals("")
                                        || textViewDate.getText().toString().equals("")) {
                                    Snackbar.make(v, "Must Enter All Details", Snackbar.LENGTH_LONG).show();
                                }
                                else {
                                    // Date selected should be today's date or sometime in the future
                                    try {
                                        Date selectedDate = new SimpleDateFormat("dd/MM/yyyy").parse(textViewDate.getText().toString());
                                        Calendar today = Calendar.getInstance();
                                        today.set(android.icu.util.Calendar.HOUR_OF_DAY, 0);
                                        today.set(android.icu.util.Calendar.MINUTE, 0);
                                        today.set(android.icu.util.Calendar.SECOND, 0);
                                        today.set(android.icu.util.Calendar.MILLISECOND, 0);
                                        if (selectedDate.before(today.getTime())) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(CreateTournament.this);
                                            builder.setTitle("Warning");
                                            builder.setMessage("Cannot Select Date Before Today's Date");
                                            builder.setNegativeButton("OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                        }
                                                    });

                                            AlertDialog chooseTeeBoxes = builder.create();
                                            chooseTeeBoxes.show();
                                        }
                                        else {
                                            Intent invitePlayersIntent = new Intent(CreateTournament.this, InvitePlayers.class);
                                            invitePlayersIntent.putExtra("tournament name", editTextTournamentName.getText().toString());
                                            invitePlayersIntent.putExtra("tournament course", courseFirebaseKey);
                                            invitePlayersIntent.putExtra("tournament date", textViewDate.getText().toString());

                                            // If creating tournament while starting play
                                            if (getIntent().hasExtra("playFragment")) {
                                                invitePlayersIntent.putExtra("playFragment", "Play Fragment");
                                            }
                                            startActivity(invitePlayersIntent);
                                        }
                                    }
                                    catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        courseName = prefs.getString("Course Name", "no course");
        courseFirebaseKey = prefs.getString("Course Firebase Key", "");
        if (!courseName.equalsIgnoreCase("no course")) {
            textViewCourseName.setText(courseName);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
