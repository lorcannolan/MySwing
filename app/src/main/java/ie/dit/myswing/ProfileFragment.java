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

    private String userAge, userTeeBox, userHandicap, userClub, userSociety;

    private Calendar calendarUserDOB = Calendar.getInstance();
    private Calendar currentDate = Calendar.getInstance();

    private TextView textViewUserName, textViewAge, textViewAvgHandicap, textViewClub, textViewSociety;
    private EditText editTextGender;
    private ImageView info;
    private FloatingActionButton changeDOB, changeClub, changeSociety;

    DatabaseReference usersRef, courseRef, societyRef;
    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        courseRef = FirebaseDatabase.getInstance().getReference().child("courses");
        societyRef = FirebaseDatabase.getInstance().getReference().child("societies");

        textViewUserName = (TextView)view.findViewById(R.id.user_name);
        editTextGender = (EditText) view.findViewById(R.id.profile_tee_box);
        textViewAvgHandicap = (TextView)view.findViewById(R.id.avg_handicap);
        info = (ImageView) view.findViewById(R.id.info);
        textViewClub = (TextView) view.findViewById(R.id.club_name);
        textViewSociety = (TextView) view.findViewById(R.id.society_name);
        textViewAge = (TextView) view.findViewById(R.id.user_age);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName = dataSnapshot.child("first name").getValue() + " " + dataSnapshot.child("last name").getValue();
                userTeeBox = dataSnapshot.child("tee box").getValue().toString();

                // getting average handicap of user
                if (dataSnapshot.child("rounds").getChildrenCount() > 2) {
                    int totalScore = 0;
                    for (DataSnapshot data : dataSnapshot.child("rounds").getChildren()) {
                        String roundID = data.getKey();
                        totalScore += Integer.parseInt(data.child("score").getValue().toString());
                    }
                    Integer avgHandicap = (totalScore / (int)dataSnapshot.getChildrenCount()) - 72;
                    if (avgHandicap < 0) {
                        avgHandicap = 0;
                    }
                    userHandicap = avgHandicap.toString();
                    info.setVisibility(View.INVISIBLE);
                }
                else {
                    userHandicap = "N/A";
                    info.setVisibility(View.VISIBLE);
                }

                // getting user club and setting chain to get society and calculate age
                getClub(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

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
                                calculateAge(false);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
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

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.getVisibility() == View.VISIBLE) {
                    Snackbar.make(view, "Must play at least 3 rounds to calculate handicap.", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        changeClub = (FloatingActionButton) view.findViewById(R.id.edit_club);
        changeClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinClubIntent = new Intent(getActivity(), JoinClub.class);
                joinClubIntent.putExtra("UID", getActivity().getIntent().getStringExtra("UID"));
                joinClubIntent.putExtra("source", "Profile");
                startActivity(joinClubIntent);
            }
        });

        changeSociety = (FloatingActionButton) view.findViewById(R.id.edit_society);
        changeSociety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinSocietyIntent = new Intent(getActivity(), JoinSociety.class);
                joinSocietyIntent.putExtra("UID", getActivity().getIntent().getStringExtra("UID"));
                startActivity(joinSocietyIntent);
            }
        });

        return view;
    }

    public void getClub(DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild("club")) {
            String clubID = dataSnapshot.child("club").getValue().toString();
            courseRef.child(clubID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userClub = dataSnapshot.child("name").getValue().toString();
                    getSociety(dataSnapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
        else {
            userClub = "Not a Club Member";
            getSociety(dataSnapshot);
        }
    }

    public void getSociety(DataSnapshot dataSnapshot) {
        if (dataSnapshot.hasChild("society")) {
            String societyID = dataSnapshot.child("society").getValue().toString();
            societyRef.child(societyID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userSociety = dataSnapshot.child("name").getValue().toString();
                    calculateAge(true);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
        else {
            userSociety = "Not a Society Member";
            calculateAge(true);
        }
    }

    public void calculateAge(final boolean initialize) {
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
                if (intAge < 0) {
                    intAge = 0;
                }
                userAge = intAge.toString();
                setAge();
                if (initialize) {
                    setValues();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void setAge() {
        textViewAge.setText(userAge);
    }

    public void setValues() {
        textViewUserName.setText(userName);
        editTextGender.setText(userTeeBox);
        textViewAvgHandicap.setText(userHandicap);
        textViewClub.setText(userClub);
        textViewSociety.setText(userSociety);
    }
}
