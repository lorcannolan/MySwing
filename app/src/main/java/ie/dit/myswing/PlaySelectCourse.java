package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlaySelectCourse extends AppCompatActivity {

    private EditText textBox;
    private ArrayList<Course> courseList = new ArrayList<>();
    private CourseListAdapter courseListAdapter;

    private ListView courseListView;
    private TextView empty;

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    private Course selectedCourse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_select_course);

        getSupportActionBar().setTitle("Add New Course");

        courseListView = (ListView)findViewById(R.id.play_course_list);
        empty = (TextView)findViewById(R.id.play_empty_text);

        loadAllCourses();

        textBox = (EditText)findViewById(R.id.play_search_text);
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
                Intent confirmRoundIntent = new Intent(PlaySelectCourse.this, ConfirmRound.class);
                confirmRoundIntent.putExtra("courseFirebaseKey", selectedCourse.getFirebaseKey());
                confirmRoundIntent.putExtra("coursePlacesID", selectedCourse.getPlacesID());
                confirmRoundIntent.putExtra("courseName", selectedCourse.getName());
                confirmRoundIntent.putExtra("courseLatitude", selectedCourse.getLatitude());
                confirmRoundIntent.putExtra("courseLongitude", selectedCourse.getLongitude());

                startActivity(confirmRoundIntent);
                finish();
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
                    long numberOfCourses = dataSnapshot.getChildrenCount();
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
                        numberOfCourses -= 1;
                        if (numberOfCourses == 0) {
                            courseListAdapter = new CourseListAdapter(PlaySelectCourse.this, R.layout.list_adapter_view, courseList);
                            courseListView.setAdapter(courseListAdapter);
                            courseListView.setDivider(null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
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
                    courseListAdapter = new CourseListAdapter(PlaySelectCourse.this, R.layout.list_adapter_view, courseList);
                    courseListView.setAdapter(courseListAdapter);
                    courseListView.setDivider(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }
}
