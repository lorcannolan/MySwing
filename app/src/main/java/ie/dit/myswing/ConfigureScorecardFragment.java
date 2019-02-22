package ie.dit.myswing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigureScorecardFragment extends Fragment {

    private Spinner genderSpinner, nineSpinner;
    private ScorecardAdapter holeNumberAdapter;
    private List<String> frontNine = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    private List<String> backNine = Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18");
    private List<String> nineHolesArrayToBePassedToAdapter = frontNine;
    private String genderSelected;

    private ListView holeListView;
    private View view;

    private AppCompatButton updateButton;

    private String courseUniqueIdentifier;
    private boolean holesFieldPresent = false;

    private DatabaseReference courseRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_configure_scorecard, container, false);

        genderSpinner = view.findViewById(R.id.choose_gender);
        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.teeBoxes, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderSpinnerAdapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String genderSpinnerText = parent.getItemAtPosition(position).toString();
                if (genderSpinnerText.equalsIgnoreCase("ladies")) {
                    genderSelected = genderSpinner.getSelectedItem().toString();
                }
                else {
                    genderSelected = genderSpinner.getSelectedItem().toString();
                }
                holeNumberAdapter = new ScorecardAdapter(getContext(), R.layout.scorecard_row, nineHolesArrayToBePassedToAdapter,
                        courseUniqueIdentifier, genderSelected);
                holeListView.setAdapter(holeNumberAdapter);
                holeListView.setDivider(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        nineSpinner = view.findViewById(R.id.choose_nine);
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
                holeNumberAdapter = new ScorecardAdapter(getContext(), R.layout.scorecard_row, nineHolesArrayToBePassedToAdapter,
                        courseUniqueIdentifier, genderSelected);
                holeListView.setAdapter(holeNumberAdapter);
                holeListView.setDivider(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        holeListView = view.findViewById(R.id.hole_number_list);

        Intent i = getActivity().getIntent();
        courseUniqueIdentifier = i.getStringExtra("courseFirebaseKey");

        courseRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseUniqueIdentifier);

        return view;
    }
}
