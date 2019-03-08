package ie.dit.myswing;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private String userID, userName, userDOB;
    private Calendar calendarUserDOB = Calendar.getInstance();
    private Calendar currentDate = Calendar.getInstance();

    private TextView textViewUserName, textViewAge, textViewAvgHandicap, textViewClub, textViewSociety;
    private EditText editTextGender;
    private ImageView info;
    private FloatingActionButton changeDOB, changeClub, changeSociety;

    DatabaseReference usersRef, courseRef;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("first name").getValue() + " " + dataSnapshot.child("last name").getValue();
                textViewUserName.setText(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        textViewUserName = (TextView)view.findViewById(R.id.user_name);
        textViewUserName.setText(userName);

        textViewAge = (TextView) view.findViewById(R.id.user_age);
        calculateAge();

        changeDOB = (FloatingActionButton) view.findViewById(R.id.edit_dob);
        changeDOB.setOnClickListener(new View.OnClickListener() {
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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // When OK is selected and date is set, EditText field is populated with the selected values
                                usersRef.child("dob").setValue(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                calculateAge();
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        editTextGender = (EditText) view.findViewById(R.id.profile_tee_box);
        usersRef.child("tee box").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                editTextGender.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        editTextGender.setInputType(InputType.TYPE_NULL);
        editTextGender.setOnClickListener(new View.OnClickListener() {
            /*
            Source code on Dialogs found at:
                - https://developer.android.com/guide/topics/ui/dialogs
                - https://developer.android.com/reference/android/app/AlertDialog.Builder
            */
            @Override
            public void onClick(View v) {
                // Custom built dialog pop-up
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Which Tee Box Do You Play From?");
                builder.setItems(R.array.teeBoxes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Resources res = getResources();
                        String[] teeBoxes = res.getStringArray(R.array.teeBoxes);
                        editTextGender.setText(teeBoxes[which]);
                        usersRef.child("tee box").setValue(teeBoxes[which]);
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

        textViewAvgHandicap = (TextView)view.findViewById(R.id.avg_handicap);
        info = (ImageView) view.findViewById(R.id.info);
        usersRef.child("rounds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 2) {
                    int totalScore = 0;
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String roundID = data.getKey();
                        totalScore += Integer.parseInt(dataSnapshot.child(roundID).child("score").getValue().toString());
                    }
                    Integer avgHandicap = (totalScore / (int)dataSnapshot.getChildrenCount()) - 72;
                    textViewAvgHandicap.setText(avgHandicap.toString());
                    info.setVisibility(View.INVISIBLE);
                }
                else {
                    textViewAvgHandicap.setText("N/A");
                    info.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.getVisibility() == View.VISIBLE) {
                    Snackbar.make(view, "Must play at least 3 rounds to calculate handicap.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        textViewClub = (TextView) view.findViewById(R.id.club_name);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("club")) {
                    String clubID = dataSnapshot.child("club").getValue().toString();
                    courseRef.child(clubID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            textViewClub.setText(dataSnapshot.child("name").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
                else {
                    textViewClub.setText("Not a Club member");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        changeClub = (FloatingActionButton) view.findViewById(R.id.edit_club);
        changeClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinClubIntent = new Intent(getActivity(), JoinClub.class);
                joinClubIntent.putExtra("UID", getActivity().getIntent().getStringExtra("UID"));
                startActivity(joinClubIntent);
            }
        });

        textViewSociety = (TextView) view.findViewById(R.id.society_name);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("society")) {
                    textViewSociety.setText(dataSnapshot.child("society").getValue().toString());
                }
                else {
                    textViewSociety.setText("Not a Society member");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        changeSociety = (FloatingActionButton) view.findViewById(R.id.edit_society);
        changeSociety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    public void calculateAge() {
        usersRef.child("dob").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String date = dataSnapshot.getValue().toString();
                String[] dobArray = date.split("/");
                calendarUserDOB.set(
                        Integer.parseInt(dobArray[2]),
                        Integer.parseInt(dobArray[1]),
                        Integer.parseInt(dobArray[0])
                );

                int age = currentDate.get(Calendar.YEAR) - calendarUserDOB.get(Calendar.YEAR);

                if (currentDate.get(Calendar.DAY_OF_YEAR) < calendarUserDOB.get(Calendar.DAY_OF_YEAR)){
                    age--;
                }

                Integer intAge = age;
                textViewAge.setText(intAge.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
