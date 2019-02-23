package ie.dit.myswing;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ConfirmRound extends AppCompatActivity {

    private EditText enterHandicap;
    private TextView selectedCourse;
    private AppCompatButton changeCourse;
    private FloatingTextButton confirmCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_round);

        Intent i = getIntent();
        String courseFirebaseKey = i.getStringExtra("courseFirebaseKey");
        String coursePlacesID = i.getStringExtra("coursePlacesID");
        String courseName = i.getStringExtra("courseName");
        String courseLatitude = i.getStringExtra("courseLatitude");
        String courseLongitude = i.getStringExtra("courseLongitude");

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
                if (enterHandicap.getText().toString().equals("")) {
                    Snackbar.make(v, "Must Enter Handicap", Snackbar.LENGTH_LONG).show();
                }
                else if (Integer.parseInt(enterHandicap.getText().toString()) < 0 || Integer.parseInt(enterHandicap.getText().toString()) > 36) {
                    Snackbar.make(v, "Invalid Handicap", Snackbar.LENGTH_LONG).show();
                }
                else {
                    Snackbar.make(v, "Handicap == OKAY", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
