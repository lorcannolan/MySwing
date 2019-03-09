package ie.dit.myswing;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class CreateTournament extends AppCompatActivity {

    private EditText editTextTournamentName, editTextTournamentType;
    private FloatingActionButton selectCourseFAB, selectDateFAB;
    private TextView textViewCourseName, textViewDate;
    private String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_tournament);

        getSupportActionBar().setTitle("Create Tournament");

        editTextTournamentName = (EditText) findViewById(R.id.tournament_name);

        textViewCourseName = (TextView) findViewById(R.id.selected_course);

        selectCourseFAB = (FloatingActionButton) findViewById(R.id.select_course_fab);
        selectCourseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectCourseIntent = new Intent(CreateTournament.this, JoinClub.class);
                selectCourseIntent.putExtra("source", "Create Tournament");
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        courseName = prefs.getString("Course Name", "no course");
        if (!courseName.equalsIgnoreCase("no course")) {
            textViewCourseName.setText(courseName);
            editor.putString("Course Name", "");
            editor.apply();
        }
    }
}
