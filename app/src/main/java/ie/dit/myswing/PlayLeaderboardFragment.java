package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PlayLeaderboardFragment extends Fragment {

    private String tournamentID;
    DatabaseReference tournamentRef;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    private ArrayList<String> userIDs = new ArrayList<>();
    private ArrayList<Integer> userScores = new ArrayList<>();
    private Map<String, Integer> leaderboard = new HashMap<>();

    private LeaderboardAdapter leaderboardAdapter;
    private ListView leaderboardListView;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_selected_tournament, container, false);

        tournamentID = getActivity().getIntent().getStringExtra("tournamentFirebaseKey");

        tournamentRef = FirebaseDatabase.getInstance().getReference().child("tournaments").child(tournamentID);
        tournamentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("scores")) {
                    for (DataSnapshot data : dataSnapshot.child("scores").getChildren()) {
                        userIDs.add(data.getKey());
                        userScores.add(Integer.parseInt(data.getValue().toString()));
                    }
                }
                else {
                    empty.setText("No Scores to Show!");
                    leaderboardListView.setEmptyView(empty);
                }
                getPlayerNames();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        leaderboardListView = (ListView) view.findViewById(R.id.leaderboard_list);
        empty = (TextView) view.findViewById(R.id.empty_scores);

        return view;
    }

    public void getPlayerNames() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 0; i < userIDs.size(); i++) {
                    String userName = dataSnapshot.child(userIDs.get(i)).child("first name").getValue().toString() + " " + dataSnapshot.child(userIDs.get(i)).child("last name").getValue().toString();
                    leaderboard.put(userName, userScores.get(i));
                }
                sortLeaderboard();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void sortLeaderboard() {
        /*
            Sorting algorithm for hashmap sourced online below:
                - https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
        */

        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer> > list = new LinkedList<Map.Entry<String, Integer> >(leaderboard.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer> >() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        ArrayList<String> sortedNames = new ArrayList<>();
        ArrayList<Integer> sortedScores = new ArrayList<>();
        for (Map.Entry<String, Integer> aa : list) {
            sortedNames.add(aa.getKey());
            sortedScores.add(aa.getValue());
        }

        leaderboardAdapter = new LeaderboardAdapter(getContext(), R.layout.leaderboard_row, sortedNames, sortedScores);
        leaderboardListView.setAdapter(leaderboardAdapter);
        leaderboardListView.setDivider(null);
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
////        super.setUserVisibleHint(isVisibleToUser);
//
//        if (isVisibleToUser) {
//            tournamentRef = FirebaseDatabase.getInstance().getReference().child("tournaments").child(tournamentID);
//            tournamentRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.hasChild("scores")) {
//                        for (DataSnapshot data : dataSnapshot.child("scores").getChildren()) {
//                            userIDs.add(data.getKey());
//                            userScores.add(Integer.parseInt(data.getValue().toString()));
//                        }
//                    }
//                    else {
//                        empty.setText("No Scores to Show!");
//                        leaderboardListView.setEmptyView(empty);
//                    }
//                    getPlayerNames();
//                }
//
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {}
//            });
//        }
//    }
}
