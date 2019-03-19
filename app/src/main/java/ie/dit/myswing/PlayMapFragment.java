package ie.dit.myswing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlayMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap myMap;

    private TextView holeScore, holePutts;
    private Spinner selectHoleSpinner;
    private FloatingActionButton markersFAB;
    private String selectedHole;
    private LatLng courseLatLng;
    private String courseName, roundID;

    private DatabaseReference holesRef, roundsRef;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    private FirebaseAuth mAuth;

    private ArrayList<Marker> holeMarkers = new ArrayList<>();

    private String tournamentFirebaseKey, tournamentName, markingUserRoundID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.play_map);
        mapFragment.getMapAsync(this);

        Intent i = getActivity().getIntent();
        if (i.hasExtra("tournamentFirebaseKey")) {
            tournamentFirebaseKey = i.getStringExtra("tournamentFirebaseKey");
            tournamentName = i.getStringExtra("tournamentName");
            markingUserRoundID = i.getStringExtra("markingID");
        }
        String courseFirebaseKey = i.getStringExtra("courseFirebaseKey");
        String coursePlacesID = i.getStringExtra("coursePlacesID");
        courseName = i.getStringExtra("courseName");
        String courseLatitude = i.getStringExtra("courseLatitude");
        String courseLongitude = i.getStringExtra("courseLongitude");
        roundID = i.getStringExtra("roundID");

        courseLatLng = new LatLng(
                Double.parseDouble(courseLatitude),
                Double.parseDouble(courseLongitude)
        );

        holesRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseFirebaseKey).child("holes");
        mAuth = FirebaseAuth.getInstance();
        if (!i.hasExtra("tournamentFirebaseKey")) {
            roundsRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("rounds").child(roundID);
        }

        selectHoleSpinner = (Spinner)view.findViewById(R.id.play_choose_hole);
        ArrayAdapter<CharSequence> holeSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.hole_numbers, android.R.layout.simple_spinner_item);
        holeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectHoleSpinner.setAdapter(holeSpinnerAdapter);
        selectHoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHole = parent.getItemAtPosition(position).toString();
                if (selectedHole.equalsIgnoreCase("-select hole-")) {
                    myMap.clear();
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(courseLatLng, 15f));
                    MarkerOptions courseLocation = new MarkerOptions()
                            .position(courseLatLng)
                            .title(courseName)
                            .draggable(false);
                    myMap.addMarker(courseLocation);
                }
                else {
                    myMap.clear();
                    holesRef.child(selectedHole).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            displayMarkers(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Button to display/hide course markers
        markersFAB = (FloatingActionButton)view.findViewById(R.id.show_markers);
        markersFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedHole.equalsIgnoreCase("-select hole-") && !holeMarkers.isEmpty()) {
                    for (int i = 0; i < holeMarkers.size(); i++) {
                        if (holeMarkers.get(i).isVisible()) {
                            holeMarkers.get(i).setVisible(false);
                        }
                        else {
                            holeMarkers.get(i).setVisible(true);
                        }
                    }
                }
            }
        });

        return view;
    }

    public void displayMarkers(DataSnapshot dataSnapshot) {
        Marker marker;
        /*
            Information relating to LatLngBounds was found here:
                - https://stackoverflow.com/questions/15540220/google-map-camera-position-on-multiple-markers
            LatLngBounds enables camera to zoom to markers and display all markers within the bounds of the screen.
         */
        if (!holeMarkers.isEmpty()) {
            holeMarkers.clear();
        }

        LatLngBounds.Builder mapBoundsBuilder = new LatLngBounds.Builder();
        boolean locationsToShow = false;
        // Men's Tee Box Marker
        LatLng mensTeeBox = new LatLng(
                Double.parseDouble(dataSnapshot.child("mens tee box").child("latitude").getValue().toString()),
                Double.parseDouble(dataSnapshot.child("mens tee box").child("longitude").getValue().toString())
        );
        if (!mensTeeBox.equals(courseLatLng)) {
            mapBoundsBuilder.include(mensTeeBox);
            locationsToShow = true;
            MarkerOptions mensTeeBoxMarker = new MarkerOptions()
                    .position(mensTeeBox)
                    .title(selectedHole + ". Men's Tee Box")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .draggable(true)
                    .visible(false);
            marker = myMap.addMarker(mensTeeBoxMarker);
            holeMarkers.add(marker);
        }

        // Ladies Tee Box Marker
        LatLng ladiesTeeBox = new LatLng(
                Double.parseDouble(dataSnapshot.child("ladies tee box").child("latitude").getValue().toString()),
                Double.parseDouble(dataSnapshot.child("ladies tee box").child("longitude").getValue().toString())
        );
        if (!ladiesTeeBox.equals(courseLatLng)) {
            mapBoundsBuilder.include(ladiesTeeBox);
            locationsToShow = true;
            MarkerOptions ladiesTeeBoxMarker = new MarkerOptions()
                    .position(ladiesTeeBox)
                    .title(selectedHole + ". Ladies Tee Box")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                    .draggable(true)
                    .visible(false);
            marker = myMap.addMarker(ladiesTeeBoxMarker);
            holeMarkers.add(marker);
        }

        // Front of Green Marker
        LatLng frontOfGreen = new LatLng(
                Double.parseDouble(dataSnapshot.child("front green").child("latitude").getValue().toString()),
                Double.parseDouble(dataSnapshot.child("front green").child("longitude").getValue().toString())
        );
        if (!frontOfGreen.equals(courseLatLng)) {
            mapBoundsBuilder.include(frontOfGreen);
            locationsToShow = true;
            MarkerOptions frontOfGreenMarker = new MarkerOptions()
                    .position(frontOfGreen)
                    .title(selectedHole + ". Front of Green")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .draggable(true)
                    .visible(false);
            marker = myMap.addMarker(frontOfGreenMarker);
            holeMarkers.add(marker);
        }

        // Middle of Green Marker
        LatLng middleOfGreen = new LatLng(
                Double.parseDouble(dataSnapshot.child("middle green").child("latitude").getValue().toString()),
                Double.parseDouble(dataSnapshot.child("middle green").child("longitude").getValue().toString())
        );
        if (!middleOfGreen.equals(courseLatLng)) {
            mapBoundsBuilder.include(middleOfGreen);
            locationsToShow = true;
            MarkerOptions middleOfGreenMarker = new MarkerOptions()
                    .position(middleOfGreen)
                    .title(selectedHole + ". Middle of Green")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .draggable(true)
                    .visible(false);
            marker = myMap.addMarker(middleOfGreenMarker);
            holeMarkers.add(marker);
        }

        // Back of Green Marker
        LatLng backOfGreen = new LatLng(
                Double.parseDouble(dataSnapshot.child("back green").child("latitude").getValue().toString()),
                Double.parseDouble(dataSnapshot.child("back green").child("longitude").getValue().toString())
        );
        if (!backOfGreen.equals(courseLatLng)) {
            mapBoundsBuilder.include(backOfGreen);
            locationsToShow = true;
            MarkerOptions backOfGreenMarker = new MarkerOptions()
                    .position(backOfGreen)
                    .title(selectedHole + ". Back of Green")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .draggable(true)
                    .visible(false);
            marker = myMap.addMarker(backOfGreenMarker);
            holeMarkers.add(marker);
        }

        if (!locationsToShow) {
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(courseLatLng, 15f));
            MarkerOptions courseLocation = new MarkerOptions()
                    .position(courseLatLng)
                    .title(courseName)
                    .draggable(false);
            myMap.addMarker(courseLocation);
            Snackbar.make(getView(), "No saved locations for hole " + selectedHole, Snackbar.LENGTH_LONG).show();
        }
        else {
            LatLngBounds mapBounds = mapBoundsBuilder.build();
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .bearing(90)
//                    .zoom(getBoundsZoomLeve)
//                    .build();
            myMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 100));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                if (!selectedHole.equalsIgnoreCase("-select hole-")) {
                    // Playing tournament and first shot is being recorded for other user so need to get the DB ref to their round
                    if (roundsRef == null) {
                        usersRef.child(markingUserRoundID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean started = false;
                                // check the user has recorded a round
                                if (dataSnapshot.hasChild("rounds")) {
                                    for (DataSnapshot data : dataSnapshot.child("rounds").getChildren()) {
                                        // If the player whose scorecard the current user is marking has begun their round, the round id will be gained
                                        if (data.hasChild("tournamentID")) {
                                            if (tournamentFirebaseKey.equals(data.child("tournamentID").getValue().toString())) {
                                                started = true;
                                                roundsRef = FirebaseDatabase.getInstance().getReference().child("users").child(markingUserRoundID).child("rounds").child(data.getKey());
                                                addShot(latLng);
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!started) {
                                    Toast.makeText(getContext(), "The user has not started their round yet", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });
                    }
                    else {
                        addShot(latLng);
                    }
                }
            }
        });
    }

    public void addShot(final LatLng latLng) {
        roundsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roundsRef.child("score").setValue(Integer.parseInt(dataSnapshot.child("score").getValue().toString()) + 1);
                if (!dataSnapshot.child("holes").exists()) {
                    roundsRef.child("holes").child(selectedHole).child("shots").child("1").child("location").setValue(latLng);
                }
                else {
                    roundsRef.child("holes").child(selectedHole).child("shots").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String shotNumber = Long.toString(dataSnapshot.getChildrenCount()) + 1;
                            roundsRef.child(roundID).child("holes").child(selectedHole).child("shots").child(shotNumber).child("location").setValue(latLng);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        MarkerOptions courseLocation = new MarkerOptions()
                .position(latLng);
        myMap.addMarker(courseLocation);
    }
}
