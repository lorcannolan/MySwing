package ie.dit.myswing;

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

import java.util.ArrayList;

public class RoundsFragment extends Fragment {

    private ArrayList<Round> roundList = new ArrayList<>();
    private RoundListAdapter roundListAdapter;

    private ListView roundListView;
    private TextView empty;

    FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_rounds, container, false);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference roundsRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("rounds");

        roundListView = (ListView)view.findViewById(R.id.round_list);
        empty = (TextView)view.findViewById(R.id.rounds_empty_text);

        roundsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    empty.setText("You have not played any Rounds of Golf!");
                    roundListView.setEmptyView(empty);
                }
                else {
                    if (!roundList.isEmpty()) {
                        roundList.clear();
                    }
                    long numberOfRounds = dataSnapshot.getChildrenCount();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Round round = new Round(
                                data.getKey(),
                                data.child("course name").getValue().toString(),
                                data.child("courseID").getValue().toString(),
                                data.child("date").getValue().toString(),
                                Integer.parseInt(data.child("handicap").getValue().toString()),
                                Integer.parseInt(data.child("score").getValue().toString()),
                                Integer.parseInt(data.child("total putts").getValue().toString())
                        );
                        roundList.add(round);
                        numberOfRounds -= 1;
                        if (numberOfRounds == 0) {
                            /*
                            This condition added in-case of rapid change of menu item or if back button is selected. Allows app to continue running as the user intends.
                            Found at the below link:
                             - https://stackoverflow.com/questions/39532507/attempt-to-invoke-virtual-method-java-lang-object-android-content-context-getsy
                             */
                            if (getActivity() != null) {
                                roundListAdapter = new RoundListAdapter(getContext(), R.layout.round_list_row, roundList);
                                roundListView.setAdapter(roundListAdapter);
                                roundListView.setDivider(null);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        return view;
    }
}
