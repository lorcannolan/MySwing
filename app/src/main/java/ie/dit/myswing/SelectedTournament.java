package ie.dit.myswing;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SelectedTournament extends AppCompatActivity {

    private String tournamentID;
    DatabaseReference tournamentRef;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    private ArrayList<String> userIDs = new ArrayList<>();
    private ArrayList<Integer> userScores = new ArrayList<>();
    private Map<String, Integer> leaderboard = new HashMap<>();

    private LeaderboardAdapter leaderboardAdapter;
    private ListView leaderboardListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_tournament);

        tournamentID = getIntent().getStringExtra("tournamentID");

        getSupportActionBar().setTitle("Selected Tournament");
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background));

        leaderboardListView = (ListView) findViewById(R.id.leaderboard_list);

        tournamentRef = FirebaseDatabase.getInstance().getReference().child("tournaments").child(tournamentID);
        tournamentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                getSupportActionBar().setTitle(dataSnapshot.child("name").getValue().toString());
                //numberOfPlayers = (int)dataSnapshot.child("scores").getChildrenCount();
                for (DataSnapshot data : dataSnapshot.child("scores").getChildren()) {
                    userIDs.add(data.getKey());
                    userScores.add(Integer.parseInt(data.getValue().toString()));
                }
                getPlayerNames();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
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
//        Iterator it = leaderboard.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            Log.d("SelectedTournament", "******************\nName: " + pair.getKey() + "\nScore: " + pair.getValue());
//        }

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

        leaderboardAdapter = new LeaderboardAdapter(this, R.layout.leaderboard_row, sortedNames, sortedScores);
        leaderboardListView.setAdapter(leaderboardAdapter);
        leaderboardListView.setDivider(null);
    }
}
