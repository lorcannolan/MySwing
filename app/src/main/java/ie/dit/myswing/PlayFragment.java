package ie.dit.myswing;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayFragment extends Fragment {

    private EditText textBox;
    private ArrayList<Course> courseList = new ArrayList<>();
    private CourseListAdapter courseListAdapter;

    private ListView courseListView;
    private TextView empty;

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    private Course selectedCourse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play, container, false);

        loadAllCourses(view);

        courseListView = (ListView)view.findViewById(R.id.play_course_list);
        empty = (TextView)view.findViewById(R.id.play_empty_text);

        loadAllCourses(view);

        textBox = (EditText)view.findViewById(R.id.play_search_text);
        textBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadSearchedCourses(view);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = (Course) parent.getItemAtPosition(position);
                Intent confirmRoundIntent = new Intent(getActivity(), ConfirmRound.class);
                confirmRoundIntent.putExtra("courseFirebaseKey", selectedCourse.getFirebaseKey());
                confirmRoundIntent.putExtra("coursePlacesID", selectedCourse.getPlacesID());
                confirmRoundIntent.putExtra("courseName", selectedCourse.getName());
                confirmRoundIntent.putExtra("courseLatitude", selectedCourse.getLatitude());
                confirmRoundIntent.putExtra("courseLongitude", selectedCourse.getLongitude());

                startActivity(confirmRoundIntent);
                getActivity().finish();
            }

        });

        return view;
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
                    courseListAdapter = new CourseListAdapter(getContext(), R.layout.list_adapter_view, courseList);
                    courseListView.setAdapter(courseListAdapter);
                    courseListView.setDivider(null);
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
                    courseListAdapter = new CourseListAdapter(getContext(), R.layout.list_adapter_view, courseList);
                    courseListView.setAdapter(courseListAdapter);
                    courseListView.setDivider(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }
}
