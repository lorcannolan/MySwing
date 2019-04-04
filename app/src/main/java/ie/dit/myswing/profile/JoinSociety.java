package ie.dit.myswing.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import ie.dit.myswing.R;
import ie.dit.myswing.java_classes.Society;
import ie.dit.myswing.greeting.Home;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class JoinSociety extends AppCompatActivity {

    private FloatingTextButton addNewSociety;
    private EditText textBox;
    private ArrayList<Society> societyList = new ArrayList<>();
    private SocietyListAdapter societyListAdapter;

    private ListView societyListView;
    private TextView empty;

    DatabaseReference societyRef = FirebaseDatabase.getInstance().getReference().child("societies");
    private DatabaseReference currentUserRef, allUsersRef;
    private FirebaseAuth mAuth;
    private String currentUserName;

    private Society selectedSociety;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_society);

        mAuth = FirebaseAuth.getInstance();
        currentUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        currentUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserName = dataSnapshot.child("first name").getValue().toString() + " " + dataSnapshot.child("last name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        getSupportActionBar().setTitle("Join Society");

        loadAllCourses();

        textBox = (EditText)findViewById(R.id.society_search_text);
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

        societyListView = (ListView)findViewById(R.id.society_list);
        empty = (TextView)findViewById(R.id.society_empty_text);

        societyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedSociety = (Society) parent.getItemAtPosition(position);
                final Intent returnToProfileIntent = new Intent(JoinSociety.this, Home.class);
                returnToProfileIntent.putExtra("fragment", "Profile");

                AlertDialog.Builder builder = new AlertDialog.Builder(JoinSociety.this);
                builder.setTitle("Are you sure you want to join " + selectedSociety.getName() + "?");
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentUserRef.child("society").setValue(selectedSociety.getFirebaseKey());
                                if (getIntent().hasExtra("source")) {
                                    if (getIntent().getStringExtra("source").equalsIgnoreCase("create tournament")) {
                                        finish();
                                    }
                                }
                                else {
                                    startActivity(returnToProfileIntent);
                                    finish();
                                }
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

        addNewSociety = (FloatingTextButton)findViewById(R.id.add_society);
        addNewSociety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = JoinSociety.this;
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText name = new EditText(JoinSociety.this);
                name.setHint("Enter Society Name");
                layout.addView(name);

                final EditText organization = new EditText(JoinSociety.this);
                organization.setHint("Enter Organization Name");
                layout.addView(organization);

                AlertDialog.Builder builder = new AlertDialog.Builder(JoinSociety.this);
                builder.setTitle("Enter Society Details");
                builder.setView(layout);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                params.setMargins(50, 10, 50, 10);
                name.setLayoutParams(params);
                organization.setLayoutParams(params);

                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String id = societyRef.push().getKey();
                                societyRef.child(id).child("name").setValue(name.getText().toString());
                                societyRef.child(id).child("organization").setValue(organization.getText().toString());
                                societyRef.child(id).child("created by").setValue(currentUserName);
                                loadAllCourses();
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

    public void loadAllCourses() {
        societyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    empty.setText("No Societies Saved to Database");
                    societyListView.setEmptyView(empty);
                }
                else {
                    if (!societyList.isEmpty()) {
                        societyList.clear();
                    }
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Society society = new Society(
                                data.getKey(),
                                data.child("name").getValue().toString(),
                                data.child("organization").getValue().toString(),
                                data.child("created by").getValue().toString()
                        );
                        societyList.add(society);
                    }
                    societyListAdapter = new SocietyListAdapter(JoinSociety.this, R.layout.society_list_adapter_view, societyList);
                    societyListView.setAdapter(societyListAdapter);
                    societyListView.setDivider(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void loadSearchedCourses() {
        if (textBox.getText().toString().equals("")) {
            loadAllCourses();
        }
        else {
            societyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!societyList.isEmpty()) {
                        societyList.clear();
                    }
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child("name").getValue().toString().toLowerCase().contains(textBox.getText().toString().toLowerCase()) ) {
                            Society society = new Society(
                                    data.getKey(),
                                    data.child("name").getValue().toString(),
                                    data.child("organization").getValue().toString(),
                                    data.child("created by").getValue().toString()
                            );
                            societyList.add(society);
                        }
                    }
                    societyListAdapter = new SocietyListAdapter(JoinSociety.this, R.layout.society_list_adapter_view, societyList);
                    societyListView.setAdapter(societyListAdapter);
                    societyListView.setDivider(null);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }
}
