package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.util.Arrays;
import java.util.List;

public class PlayScorecardFragment extends Fragment {

    private Spinner nineSpinner;
    private PlayScorecardAdapter holeDataAdapter;
    private List<String> frontNine = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    private List<String> backNine = Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18");
    private List<String> nineHolesArrayToBePassedToAdapter = frontNine;

    private ListView holeDataListView;

    private TextView score;

    private String userGender, courseUniqueIdentifier, roundID;
    private DatabaseReference courseRef, roundRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play_scorecard, container, false);

        Intent i = getActivity().getIntent();
        userGender = i.getStringExtra("userGender");
        courseUniqueIdentifier = i.getStringExtra("courseFirebaseKey");
        roundID = i.getStringExtra("roundID");

        nineSpinner = view.findViewById(R.id.play_choose_nine);
        ArrayAdapter<CharSequence> nineSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.nines, android.R.layout.simple_spinner_item);
        nineSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nineSpinner.setAdapter(nineSpinnerAdapter);
        nineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nineSpinnerText = parent.getItemAtPosition(position).toString();
                if (nineSpinnerText.equals("Front 9")) {
                    nineHolesArrayToBePassedToAdapter = frontNine;
                }
                else {
                    nineHolesArrayToBePassedToAdapter = backNine;
                }
                holeDataAdapter = new PlayScorecardAdapter(getContext(), R.layout.play_scorecard_row, nineHolesArrayToBePassedToAdapter,
                        courseUniqueIdentifier, userGender, roundID);
                holeDataListView.setAdapter(holeDataAdapter);
                holeDataListView.setDivider(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        score = (TextView)view.findViewById(R.id.round_score);
        mAuth = FirebaseAuth.getInstance();
        roundRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("rounds").child(roundID);
        roundRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                score.setText(dataSnapshot.child("score").getValue().toString());
                holeDataAdapter = new PlayScorecardAdapter(getContext(), R.layout.play_scorecard_row, nineHolesArrayToBePassedToAdapter,
                        courseUniqueIdentifier, userGender, roundID);
                holeDataListView.setAdapter(holeDataAdapter);
                holeDataListView.setDivider(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        holeDataListView = view.findViewById(R.id.play_hole_data_list);

        courseRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseUniqueIdentifier);

        return view;
    }
}
