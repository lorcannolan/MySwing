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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
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

import java.util.Calendar;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class CreateTournament extends AppCompatActivity {

    private FloatingTextButton confirm;
    private EditText editTextTournamentName, editTextTournamentType;
    private FloatingActionButton selectCourseFAB, selectDateFAB;
    private TextView textViewCourseName, textViewDate;
    private String courseName;

    DatabaseReference userRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tournament);

        getSupportActionBar().setTitle("Create Tournament");

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        editTextTournamentName = (EditText) findViewById(R.id.tournament_name);

        textViewCourseName = (TextView) findViewById(R.id.selected_course);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (getIntent().hasExtra("tournamentFragment")) {
            editor.putString("Course Name", "");
            editor.apply();
        }
        courseName = prefs.getString("Course Name", "no course");
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
            public void onClick(View v) {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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

                        if (editTextTournamentType.getText().toString().equalsIgnoreCase("society")
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        courseName = prefs.getString("Course Name", "no course");
        if (!courseName.equalsIgnoreCase("no course")) {
            textViewCourseName.setText(courseName);
        }
    }
}
