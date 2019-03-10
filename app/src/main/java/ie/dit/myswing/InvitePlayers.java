package ie.dit.myswing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class InvitePlayers extends AppCompatActivity {

    private ArrayList<User> userList = new ArrayList<>();
    private UserListAdapter userListAdapter;

    private ListView userListView;
    private TextView empty;

    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

    private User selectedUser;
    private ArrayList<User> selectedUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_players);

        Log.d("InvitePlayers",
                "****************\nTournament Name: " + getIntent().getStringExtra("tournament name")
                    + "\nTournament Course: " + getIntent().getStringExtra("tournament course")
                    + "\nTournament Date: " + getIntent().getStringExtra("tournament date")
        );

        getSupportActionBar().setTitle("Invite Players");

        userListView = (ListView) findViewById(R.id.user_list);
        empty = (TextView) findViewById(R.id.user_empty_text);

        loadAllCourses();

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedUser = (User) parent.getItemAtPosition(position);
                if (selectedUsersList.contains(selectedUser)) {
                    selectedUsersList.remove(selectedUser);
                }
                else {
                    selectedUsersList.add(selectedUser);
                }
                userListAdapter.highlightCard(position);
            }
        });
    }

    public void loadAllCourses() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    empty.setText("No Courses Saved to Database");
                    userListView.setEmptyView(empty);
                }
                else {
                    if (!userList.isEmpty()) {
                        userList.clear();
                    }
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.hasChild("club") && data.hasChild("society")) {
                            User user = new User(
                                    data.getKey(),
                                    data.child("first name").getValue().toString(),
                                    data.child("last name").getValue().toString(),
                                    data.child("dob").getValue().toString(),
                                    data.child("club").getValue().toString(),
                                    data.child("society").getValue().toString(),
                                    data.child("tee box").getValue().toString()
                            );
                            userList.add(user);
                        }
                        else if (data.hasChild("club") && !data.hasChild("society")) {
                            User user = new User(
                                    data.getKey(),
                                    data.child("first name").getValue().toString(),
                                    data.child("last name").getValue().toString(),
                                    data.child("dob").getValue().toString(),
                                    data.child("club").getValue().toString(),
                                    "None",
                                    data.child("tee box").getValue().toString()
                            );
                            userList.add(user);
                        }
                        else if (!data.hasChild("club") && data.hasChild("society")) {
                            User user = new User(
                                    data.getKey(),
                                    data.child("first name").getValue().toString(),
                                    data.child("last name").getValue().toString(),
                                    data.child("dob").getValue().toString(),
                                    "None",
                                    data.child("society").getValue().toString(),
                                    data.child("tee box").getValue().toString()
                            );
                            userList.add(user);
                        }
                        else if (!data.hasChild("club") && !data.hasChild("society")) {
                            User user = new User(
                                    data.getKey(),
                                    data.child("first name").getValue().toString(),
                                    data.child("last name").getValue().toString(),
                                    data.child("dob").getValue().toString(),
                                    "None",
                                    "None",
                                    data.child("tee box").getValue().toString()
                            );
                            userList.add(user);
                        }
                    }
                    userListAdapter = new UserListAdapter(InvitePlayers.this, R.layout.user_list_row, userList);
                    userListView.setAdapter(userListAdapter);
                    userListView.setDivider(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
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
