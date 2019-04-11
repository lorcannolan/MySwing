package ie.dit.myswing.tournaments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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

import ie.dit.myswing.play.PlaySelectTournament;
import ie.dit.myswing.R;
import ie.dit.myswing.java_classes.User;
import ie.dit.myswing.greeting.Home;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class InvitePlayers extends AppCompatActivity {

    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<User> userSearchList = new ArrayList<>();
    private UserListAdapter userListAdapter;

    private ListView userListView;
    private TextView empty;
    private EditText searchUsersEditText;
    private FloatingTextButton confirmTournament;

    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference currentUserRef, tournamentsRef;
    FirebaseAuth mAuth;
    private String currentUserName;

    private User selectedUser, currentUser;
    private ArrayList<User> selectedUsersList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_players);

        getSupportActionBar().setTitle("Invite Players");

        tournamentsRef = FirebaseDatabase.getInstance().getReference().child("tournaments");

        mAuth = FirebaseAuth.getInstance();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        currentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserName = dataSnapshot.child("first name").getValue().toString().toLowerCase()
                        + " " + dataSnapshot.child("last name").getValue().toString().toLowerCase();
                if (dataSnapshot.hasChild("club") && dataSnapshot.hasChild("society")) {
                    currentUser = new User(
                            mAuth.getCurrentUser().getUid(),
                            dataSnapshot.child("first name").getValue().toString(),
                            dataSnapshot.child("last name").getValue().toString(),
                            dataSnapshot.child("dob").getValue().toString(),
                            dataSnapshot.child("club").getValue().toString(),
                            dataSnapshot.child("society").getValue().toString(),
                            dataSnapshot.child("tee box").getValue().toString()
                    );
                }
                else if (dataSnapshot.hasChild("club") && !dataSnapshot.hasChild("society")) {
                    currentUser = new User(
                            mAuth.getCurrentUser().getUid(),
                            dataSnapshot.child("first name").getValue().toString(),
                            dataSnapshot.child("last name").getValue().toString(),
                            dataSnapshot.child("dob").getValue().toString(),
                            dataSnapshot.child("club").getValue().toString(),
                            "None",
                            dataSnapshot.child("tee box").getValue().toString()
                    );
                }
                else if (!dataSnapshot.hasChild("club") && dataSnapshot.hasChild("society")) {
                    currentUser = new User(
                            mAuth.getCurrentUser().getUid(),
                            dataSnapshot.child("first name").getValue().toString(),
                            dataSnapshot.child("last name").getValue().toString(),
                            dataSnapshot.child("dob").getValue().toString(),
                            "None",
                            dataSnapshot.child("society").getValue().toString(),
                            dataSnapshot.child("tee box").getValue().toString()
                    );
                }
                else if (!dataSnapshot.hasChild("club") && !dataSnapshot.hasChild("society")) {
                    currentUser = new User(
                            mAuth.getCurrentUser().getUid(),
                            dataSnapshot.child("first name").getValue().toString(),
                            dataSnapshot.child("last name").getValue().toString(),
                            dataSnapshot.child("dob").getValue().toString(),
                            "None",
                            "None",
                            dataSnapshot.child("tee box").getValue().toString()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        userListView = (ListView) findViewById(R.id.user_list);
        empty = (TextView) findViewById(R.id.user_empty_text);

        loadAllUsers();

        searchUsersEditText = (EditText)findViewById(R.id.user_search_text);
        searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadSearchedUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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
                userListAdapter.checkCard(position);
            }
        });

        confirmTournament = (FloatingTextButton)findViewById(R.id.confirm_invite_players);
        confirmTournament.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUsersList.isEmpty()) {
                    Snackbar.make(v, "Must Select Users to Invite", Snackbar.LENGTH_LONG).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(InvitePlayers.this);
                    builder.setTitle("Confirm Players To Invite");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedUsersList.add(currentUser);
                                    String id = tournamentsRef.push().getKey();
                                    tournamentsRef.child(id).child("name").setValue(getIntent().getStringExtra("tournament name"));
                                    tournamentsRef.child(id).child("course").setValue(getIntent().getStringExtra("tournament course"));
                                    tournamentsRef.child(id).child("date").setValue(getIntent().getStringExtra("tournament date"));
                                    for (User u : selectedUsersList) {
                                        String invitedID = tournamentsRef.child(id).child("invited").push().getKey();
                                        tournamentsRef.child(id).child("invited").child(invitedID).child("userID").setValue(u.getFirebaseKey());
                                    }

                                    // If creating tournament while starting play
                                    if (getIntent().hasExtra("playFragment")) {
                                        Intent returnToPlaySetup = new Intent(InvitePlayers.this, PlaySelectTournament.class);
                                        startActivity(returnToPlaySetup);
                                        finish();
                                    }
                                    // If creating tournament from within tournaments
                                    else {
                                        Intent backToTournamentsIntent = new Intent(InvitePlayers.this, Home.class);
                                        backToTournamentsIntent.putExtra("fragment", "Tournaments");
                                        startActivity(backToTournamentsIntent);
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
        });
    }

    public void loadAllUsers() {
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
                        // Don't add current user
                        if (!currentUserName.equalsIgnoreCase(data.child("first name").getValue().toString() + " " + data.child("last name").getValue().toString())) {
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

    public void loadSearchedUsers () {
        if (searchUsersEditText.getText().toString().equals("")) {
            loadAllUsers();
        }
        else {
            if (!userSearchList.isEmpty()) {
                userSearchList.clear();
            }
            for (User u : userList) {
                String fullName = u.getFirstName().toLowerCase() + " " + u.getLastName().toLowerCase();
                if (fullName.contains(searchUsersEditText.getText().toString().toLowerCase())) {
                    userSearchList.add(u);
                }
            }
            userListAdapter = new UserListAdapter(InvitePlayers.this, R.layout.user_list_row, userSearchList);
            userListView.setAdapter(userListAdapter);
            userListView.setDivider(null);
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
