package ie.dit.myswing.tournaments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.Date;

import ie.dit.myswing.R;
import ie.dit.myswing.java_classes.Tournament;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class PlayedTournamentsFragment extends Fragment {

    private ArrayList<Tournament> tournamentList = new ArrayList<>();
    private TournamentListAdapter tournamentListAdapter;

    private ListView tournamentListView;
    private TextView empty;

    DatabaseReference coursesRef = FirebaseDatabase.getInstance().getReference().child("courses");
    DatabaseReference tournamentsRef = FirebaseDatabase.getInstance().getReference().child("tournaments");
    DatabaseReference societiesRef = FirebaseDatabase.getInstance().getReference().child("societies");
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    DatabaseReference roundsRef;

    FirebaseAuth mAuth;
    DataSnapshot coursesSnapshot;

    private String userClub, userSociety;
    private int numberOfTournaments;

    private boolean hasPlayedTournament = false;

    private ArrayList<String> tournamentIDs = new ArrayList<>();

    private Tournament selectedTournament;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_played_tournaments, container, false);

        tournamentIDs.clear();

        tournamentListView = (ListView)view.findViewById(R.id.played_tournaments_list);
        empty = (TextView)view.findViewById(R.id.played_empty_text);

        mAuth = FirebaseAuth.getInstance();
        roundsRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("rounds");
        roundsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.hasChild("tournamentID")) {
                            hasPlayedTournament = true;
                            tournamentIDs.add(data.child("tournamentID").getValue().toString());
                        }
                    }
                    if (tournamentIDs.size() == 0) {
                        empty.setText("No Upcoming Tournaments to Show");
                        tournamentListView.setEmptyView(empty);
                    }
                    else {
                        loadTournaments();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                coursesSnapshot = dataSnapshot;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        FloatingTextButton newTournament = (FloatingTextButton)view.findViewById(R.id.play_new_tournament);
        newTournament.setVisibility(View.INVISIBLE);

        tournamentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTournament = (Tournament) parent.getItemAtPosition(position);
                Intent showLeaderboardIntent = new Intent(getActivity(), SelectedTournament.class);
                showLeaderboardIntent.putExtra("tournamentID", selectedTournament.getFirebaseKey());
                startActivity(showLeaderboardIntent);
            }
        });

        return view;
    }

    public void loadTournaments() {
        tournamentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                numberOfTournaments = tournamentIDs.size();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (tournamentIDs.contains(data.getKey())) {
                        try {
                            Date tournamentDate = new SimpleDateFormat("dd/MM/yyyy").parse(data.child("date").getValue().toString());
                            Tournament tournament = new Tournament(
                                    data.getKey(),
                                    data.child("name").getValue().toString(),
                                    data.child("course").getValue().toString(),
                                    data.child("date").getValue().toString()
                            );
                            addTournaments(tournament);
                        }
                        catch (ParseException e) {
                            e.printStackTrace();
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
        numberOfTournaments--;
        if (numberOfTournaments == 0) {
            /*
            This condition added in-case of rapid change of menu item or if back button is selected. Allows app to continue running as the user intends.
            Found at the below link:
             - https://stackoverflow.com/questions/39532507/attempt-to-invoke-virtual-method-java-lang-object-android-content-context-getsy
             */
            if (getActivity() != null) {
                tournamentListAdapter = new TournamentListAdapter(getContext(), R.layout.list_adapter_view, tournamentList);
                tournamentListView.setAdapter(tournamentListAdapter);
                tournamentListView.setDivider(null);
            }
        }
    }

}