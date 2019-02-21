package ie.dit.myswing;

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

import java.util.Arrays;
import java.util.List;

public class ConfigureScorecardFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Spinner genderSpinner, nineSpinner;
    private ScorecardAdapter holeNumberAdapter;
    private List<String> frontNine = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
    private List<String> backNine = Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "18");

    private ListView holeListView;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_configure_scorecard, container, false);

        genderSpinner = view.findViewById(R.id.choose_gender);
        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.teeBoxes, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderSpinnerAdapter);

        nineSpinner = view.findViewById(R.id.choose_nine);
        ArrayAdapter<CharSequence> nineSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.nines, android.R.layout.simple_spinner_item);
        nineSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        nineSpinner.setAdapter(nineSpinnerAdapter);
        nineSpinner.setOnItemSelectedListener(this);

        holeListView = view.findViewById(R.id.hole_number_list);

        return view;
    }

    public void loadFrontNine() {
        holeNumberAdapter = new ScorecardAdapter(getContext(), R.layout.scorecard_row, frontNine);
        holeListView.setAdapter(holeNumberAdapter);
        holeListView.setDivider(null);
    }

    public void loadBackNine() {
        holeNumberAdapter = new ScorecardAdapter(getContext(), R.layout.scorecard_row, backNine);
        holeListView.setAdapter(holeNumberAdapter);
        holeListView.setDivider(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerText = parent.getItemAtPosition(position).toString();
        if (spinnerText.equals("Front 9")) {
            loadFrontNine();
        }
        else {
            loadBackNine();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
