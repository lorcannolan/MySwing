package ie.dit.myswing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class JoinClub extends AppCompatActivity {

    private FloatingTextButton addNewCourse;
    private EditText textBox;
    private ArrayList<Course> courseList = new ArrayList<>();
    private CourseListAdapter courseListAdapter;

    private ListView courseListView;
    private TextView empty;

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    private Course selectedCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());

        getSupportActionBar().setTitle("Join Club");

        courseListView = (ListView)findViewById(R.id.course_list);
        empty = (TextView)findViewById(R.id.empty_text);

        loadAllCourses();

        textBox = (EditText)findViewById(R.id.search_text);
        textBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadSearchedCourses();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = (Course) parent.getItemAtPosition(position);
                final Intent returnToProfileIntent = new Intent(JoinClub.this, Home.class);
                returnToProfileIntent.putExtra("fragment", "Profile");

                AlertDialog.Builder builder = new AlertDialog.Builder(JoinClub.this);
                builder.setTitle("Are you sure you want to join " + selectedCourse.getName() + "?");
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userRef.child("club").setValue(selectedCourse.getFirebaseKey());
                                startActivity(returnToProfileIntent);
                                finish();
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
        });
    }

    public void loadAllCourses() {
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    empty.setText("No Courses Saved to Database");
                    courseListView.setEmptyView(empty);
                }
                else {
                    if (!courseList.isEmpty()) {
                        courseList.clear();
                    }
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Course course = new Course(
                                data.getKey(),
                                data.child("placesID").getValue().toString(),
                                data.child("name").getValue().toString(),
                                data.child("Address").getValue().toString(),
                                data.child("website").getValue().toString(),
                                data.child("location").child("latitude").getValue().toString(),
                                data.child("location").child("longitude").getValue().toString()
                        );
                        courseList.add(course);
                    }
                    courseListAdapter = new CourseListAdapter(JoinClub.this, R.layout.list_adapter_view, courseList);
                    courseListView.setAdapter(courseListAdapter);
                    courseListView.setDivider(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        addNewCourse = (FloatingTextButton)findViewById(R.id.add_course);
        addNewCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(JoinClub.this, AddCourse.class);
                mapIntent.putExtra("source", "JoinClub");
                startActivity(mapIntent);
                finish();
            }
        });
    }

    public void loadSearchedCourses () {
        if (textBox.getText().toString().equals("")) {
            loadAllCourses();
        }
        else {
            courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!courseList.isEmpty()) {
                        courseList.clear();
                    }
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child("name").getValue().toString().toLowerCase().contains(textBox.getText().toString().toLowerCase()) ) {
                            Course course = new Course(
                                    data.getKey(),
                                    data.child("placesID").getValue().toString(),
                                    data.child("name").getValue().toString(),
                                    data.child("Address").getValue().toString(),
                                    data.child("website").getValue().toString(),
                                    data.child("location").child("latitude").getValue().toString(),
                                    data.child("location").child("longitude").getValue().toString()
                            );
                            courseList.add(course);
                        }
                    }
                    courseListAdapter = new CourseListAdapter(JoinClub.this, R.layout.list_adapter_view, courseList);
                    courseListView.setAdapter(courseListAdapter);
                    courseListView.setDivider(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }
}
