package ie.dit.myswing.tournaments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import ie.dit.myswing.R;
import ie.dit.myswing.java_classes.Tournament;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class InvitedTournamentsFragment extends Fragment {

    private ArrayList<Tournament> tournamentList = new ArrayList<>();
    private TournamentListAdapter tournamentListAdapter;

    private ListView tournamentListView;
    private TextView empty;

    DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference().child("courses");
    DatabaseReference tournamentsRef = FirebaseDatabase.getInstance().getReference().child("tournaments");
    DatabaseReference societiesRef = FirebaseDatabase.getInstance().getReference().child("societies");
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");

    FirebaseAuth mAuth;
    DataSnapshot coursesSnapshot;

    private String userClub, userSociety;
    long numberOfTournaments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_invited_tournaments, container, false);

        mAuth = FirebaseAuth.getInstance();

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                coursesSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        usersRef.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("club")) {
                    userClub = dataSnapshot.child("club").getValue().toString();
                }
                else {
                    userClub = "none";
                }

                if (dataSnapshot.hasChild("society")) {
                    userSociety = dataSnapshot.child("society").getValue().toString();
                }
                else {
                    userSociety = "none";
                }
                loadTournaments();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        tournamentListView = (ListView)view.findViewById(R.id.invited_tournaments_list);
        empty = (TextView)view.findViewById(R.id.invited_empty_text);

        FloatingTextButton newTournament = (FloatingTextButton)view.findViewById(R.id.play_new_tournament);
        newTournament.setVisibility(View.INVISIBLE);

        return view;
    }

    public void loadTournaments() {
        tournamentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    empty.setText("No Upcoming Tournaments to Show");
                    tournamentListView.setEmptyView(empty);
                }
                else {
                    if (!tournamentList.isEmpty()) {
                        tournamentList.clear();
                    }
                    numberOfTournaments = dataSnapshot.getChildrenCount();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Calendar today = Calendar.getInstance();
                        today.set(android.icu.util.Calendar.HOUR_OF_DAY, 0);
                        today.set(android.icu.util.Calendar.MINUTE, 0);
                        today.set(android.icu.util.Calendar.SECOND, 0);
                        today.set(android.icu.util.Calendar.MILLISECOND, 0);
                        try {
                            Date tournamentDate = new SimpleDateFormat("dd/MM/yyyy").parse(data.child("date").getValue().toString());
                            // Only show tournaments that are occurring today
                            if (tournamentDate.after(today.getTime()) || tournamentDate.equals(today.getTime())) {
                                Tournament tournament = new Tournament(
                                        data.getKey(),
                                        data.child("name").getValue().toString(),
                                        data.child("course").getValue().toString(),
                                        data.child("date").getValue().toString()
                                );
                                if (data.hasChild("club")) {
                                    // If the current user is a member of the club, show tournament
                                    if (userClub.equals(data.child("club").getValue().toString())) {
                                        tournament.setClub(data.child("club").getValue().toString());
                                        addTournaments(tournament);
                                    }
                                } else if (data.hasChild("society")) {
                                    // If the current user is a member of the society, show tournament
                                    if (userSociety.equals(data.child("society").getValue().toString())) {
                                        tournament.setSociety(data.child("society").getValue().toString());
                                        addTournaments(tournament);
                                    }
                                } else if (data.hasChild("invited")) {
                                    int i = 0;
                                    long numberOfInvitedUsers = data.child("invited").getChildrenCount();
                                    String[] invitedUserIDs = new String[(int) numberOfInvitedUsers];
                                    // If the current user is in the list of invited users, show tournament
                                    for (DataSnapshot invitedInstance : data.child("invited").getChildren()) {
                                        if (invitedInstance.child("userID").getValue().toString().equals(mAuth.getCurrentUser().getUid())) {
                                            tournament.setInvited(invitedUserIDs);
                                            addTournaments(tournament);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (tournamentList.size() == 0) {
                        empty.setText("No Upcoming Tournaments to Show");
                        tournamentListView.setEmptyView(empty);
                    }
                    else {
                        /*
                            This condition added in-case of rapid change of menu item or if back button is selected. Allows app to continue running as the user intends.
                            Found at the below link:
                             - https://stackoverflow.com/questions/39532507/attempt-to-invoke-virtual-method-java-lang-object-android-content-context-getsy
                         */
                        if (getActivity() != null) {
                            tournamentListAdapter = new TournamentListAdapter(getContext(), R.layout.course_list_row, tournamentList);
                            tournamentListView.setAdapter(tournamentListAdapter);
                            tournamentListView.setDivider(null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void addTournaments(Tournament tournament) {
        // Set course name before adding tournament to array list
        for (DataSnapshot data : coursesSnapshot.getChildren()) {
            if (tournament.getCourseID().equals(data.getKey())) {
                tournament.setCourseName(data.child("name").getValue().toString());
                break;
            }
        }
        tournamentList.add(tournament);
    }

}
