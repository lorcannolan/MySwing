package ie.dit.myswing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ConfigureMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap myMap;

    private LatLng courseLatLng;
    private DatabaseReference holesRef;

    private String courseFirebaseKey;
    private Spinner selectHoleSpinner;
    private RelativeLayout spinnerContainer;

    private static final String TAG = "ConfigureMapFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_configure_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.add_course_locations_map);
        mapFragment.getMapAsync(this);

        Intent i = getActivity().getIntent();
        courseFirebaseKey = i.getStringExtra("courseFirebaseKey");
        String coursePlacesID = i.getStringExtra("coursePlacesID");
        String courseName = i.getStringExtra("courseName");
        String courseLatitude = i.getStringExtra("courseLatitude");
        String courseLongitude = i.getStringExtra("courseLongitude");

        holesRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseFirebaseKey).child("holes");

        spinnerContainer = (RelativeLayout)view.findViewById(R.id.spinner_container);
        spinnerContainer.getBackground().setAlpha(225);

        selectHoleSpinner = (Spinner)view.findViewById(R.id.choose_hole);
        ArrayAdapter<CharSequence> holeSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.hole_numbers, android.R.layout.simple_spinner_item);
        holeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectHoleSpinner.setAdapter(holeSpinnerAdapter);
        selectHoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedHole = parent.getItemAtPosition(position).toString();
                if (selectedHole.equalsIgnoreCase("-select hole-")) {
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(courseLatLng, 15f));
                    MarkerOptions courseLocation = new MarkerOptions()
                            .position(courseLatLng);
                    myMap.addMarker(courseLocation);
                }
                else {
                    myMap.clear();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        courseLatLng = new LatLng(
                Double.parseDouble(courseLatitude),
                Double.parseDouble(courseLongitude)
        );

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MarkerOptions droppedMarker = new MarkerOptions()
                        .position(latLng);
                myMap.addMarker(droppedMarker);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Select the Location Type for this Marker.");
                builder.setItems(R.array.location_type, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Resources res = getContext().getResources();
                        String[] locationType = res.getStringArray(R.array.location_type);
                        // Sets the text of the edit text widget that was clicked by using an ArrayList to store all of the individual edit texts
                        String chosenLocationType = (locationType[which]);
                    }
                });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog chooselocationType = builder.create();
                chooselocationType.show();
            }
        });
    }
}
