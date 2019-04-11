package ie.dit.myswing.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ie.dit.myswing.java_classes.Course;
import ie.dit.myswing.R;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class MapFragment extends Fragment {

    private FloatingTextButton addNewCourse;
    private EditText textBox;
    private ArrayList<Course> courseList = new ArrayList<>();
    private CourseListAdapter courseListAdapter;

    private ListView courseListView;
    private TextView empty;

    private boolean holesFieldPresent = false;

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    private Course selectedCourse;
    private Map<String, Object> postValues;

    private static final String TAG = "MapFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map, container, false);

        addNewCourse = (FloatingTextButton) view.findViewById(R.id.add_course);
        addNewCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(getActivity(), AddCourse.class);
                mapIntent.putExtra("source", "Map");
                startActivity(mapIntent);
                getActivity().finish();
            }
        });

        courseListView = (ListView)view.findViewById(R.id.course_list);
        empty = (TextView)view.findViewById(R.id.empty_text);

        loadAllCourses(view);

        textBox = (EditText)view.findViewById(R.id.search_text);
        textBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadSearchedCourses(view);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = (Course) parent.getItemAtPosition(position);
                Intent configureCourseIntent = new Intent(getActivity(), ConfigureCourse.class);
                configureCourseIntent.putExtra("courseFirebaseKey", selectedCourse.getFirebaseKey());
                configureCourseIntent.putExtra("coursePlacesID", selectedCourse.getPlacesID());
                configureCourseIntent.putExtra("courseName", selectedCourse.getName());
                configureCourseIntent.putExtra("courseLatitude", selectedCourse.getLatitude());
                configureCourseIntent.putExtra("courseLongitude", selectedCourse.getLongitude());

                // Add new default data for each hole to database if it doesn't already exist
                courseRef.child(selectedCourse.getFirebaseKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        postValues = new HashMap<String, Object>();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            postValues.put(data.getKey(), data.getValue());
                            if (data.getKey().equalsIgnoreCase("holes")) {
                                holesFieldPresent = true;
                            }
                        }

                        if (!holesFieldPresent) {
                            addDefaultData();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

                startActivity(configureCourseIntent);
                //getActivity().finish();
            }
        });

        return view;
    }

    public void addDefaultData() {
        Map<String, Object> holes = new HashMap<String, Object>();
        for (int i = 1; i <= 18; i++) {
            Map<String, Object> hole = new HashMap<String, Object>();
            hole.put("number", i);
            hole.put("mens par", 3);
            hole.put("ladies par", 3);
            hole.put("mens index", 1);
            hole.put("ladies index", 1);
            hole.put("mens tee box", new LatLng(
                    Double.parseDouble(selectedCourse.getLatitude()),
                    Double.parseDouble(selectedCourse.getLongitude())
                )
            );
            hole.put("ladies tee box", new LatLng(
                            Double.parseDouble(selectedCourse.getLatitude()),
                            Double.parseDouble(selectedCourse.getLongitude())
                    )
            );
            hole.put("front green", new LatLng(
                            Double.parseDouble(selectedCourse.getLatitude()),
                            Double.parseDouble(selectedCourse.getLongitude())
                    )
            );
            hole.put("middle green", new LatLng(
                            Double.parseDouble(selectedCourse.getLatitude()),
                            Double.parseDouble(selectedCourse.getLongitude())
                    )
            );
            hole.put("back green", new LatLng(
                            Double.parseDouble(selectedCourse.getLatitude()),
                            Double.parseDouble(selectedCourse.getLongitude())
                    )
            );
            holes.put(i + "", hole);
        }
        postValues.put("holes", holes);
        courseRef.child(selectedCourse.getFirebaseKey()).updateChildren(postValues);
    }

    public void loadAllCourses(View view) {
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
                            /*
                                This condition added in-case of rapid change of menu item or if back button is selected. Allows app to continue running as the user intends.
                                Found at the below link:
                                 - https://stackoverflow.com/questions/39532507/attempt-to-invoke-virtual-method-java-lang-object-android-content-context-getsy
                            */
                            if (getActivity() != null) {
                                courseListAdapter = new CourseListAdapter(getContext(), R.layout.course_list_row, courseList);
                                courseListView.setAdapter(courseListAdapter);
                                courseListView.setDivider(null);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void loadSearchedCourses (View view) {
        if (textBox.getText().toString().equals("")) {
            loadAllCourses(view);
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
                    courseListAdapter = new CourseListAdapter(getContext(), R.layout.course_list_row, courseList);
                    courseListView.setAdapter(courseListAdapter);
                    courseListView.setDivider(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

}
