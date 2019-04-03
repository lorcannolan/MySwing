package ie.dit.myswing;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class RoundMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap myMap;

    private TextView holeScore, holePutts, holePar;
    private ImageView addPutt;
    private int holePuttsInt, holeIndex, handicap, netReduction;
    private Spinner selectHoleSpinner;
    private FloatingActionButton markersFAB, infoFAB;
    private String selectedHole;
    private LatLng courseLatLng;
    private String courseName, roundID, userGender;

    private DatabaseReference holesRef, roundsRef;
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    private FirebaseAuth mAuth;

    private ArrayList<Marker> holeMarkers = new ArrayList<>();
    private ArrayList<Marker> shotMarkers = new ArrayList<>();

    private String tournamentFirebaseKey, tournamentName, markingUserRoundID, firebaseTeeBoxPath;

    private Location frontLocation, middleLocation, backLocation;

    private Bitmap shotIcon, puttIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.play_map);
        mapFragment.getMapAsync(this);

        BitmapDrawable shotBitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.golf_shot_icon);
        Bitmap shotBitmap = shotBitmapDrawable.getBitmap();
        shotIcon = Bitmap.createScaledBitmap(shotBitmap, 100, 100, false);

        BitmapDrawable puttBitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.drawable.golf_putt_icon);
        Bitmap puttBitmap = puttBitmapDrawable.getBitmap();
        puttIcon = Bitmap.createScaledBitmap(puttBitmap, 100, 100, false);

        Intent i = getActivity().getIntent();
        String courseFirebaseKey = i.getStringExtra("courseID");
        courseName = i.getStringExtra("courseName");
        String courseLatitude = i.getStringExtra("courseLatitude");
        String courseLongitude = i.getStringExtra("courseLongitude");
        roundID = i.getStringExtra("roundKey");
        userGender = i.getStringExtra("userGender");

        firebaseTeeBoxPath = "";
        if (userGender.equalsIgnoreCase("ladies")) {
            firebaseTeeBoxPath = "ladies tee box";
        }
        else {
            firebaseTeeBoxPath = "mens tee box";
        }

        courseLatLng = new LatLng(
                Double.parseDouble(courseLatitude),
                Double.parseDouble(courseLongitude)
        );

        holeScore = (TextView) view.findViewById(R.id.score_number);
        holePar = (TextView) view.findViewById(R.id.par_number);

        holesRef = FirebaseDatabase.getInstance().getReference().child("courses").child(courseFirebaseKey).child("holes");
        mAuth = FirebaseAuth.getInstance();
        if (!i.hasExtra("tournamentFirebaseKey")) {
            roundsRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid()).child("rounds").child(roundID);
            handicap = i.getIntExtra("handicap", 0);
        }

        // Button to display hole distances, etc
        infoFAB = (FloatingActionButton) view.findViewById(R.id.play_information);
        infoFAB.setVisibility(View.INVISIBLE);

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
                    infoFAB.setVisibility(View.INVISIBLE);
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(courseLatLng, 15f));
                    MarkerOptions courseLocation = new MarkerOptions()
                            .position(courseLatLng)
                            .title(courseName)
                            .draggable(false);
                    myMap.addMarker(courseLocation);
                    holePar.setText("");
                    holeScore.setText("");
                }
                else {
                    myMap.clear();
                    shotMarkers.clear();
                    holesRef.child(selectedHole).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            displayMarkers(dataSnapshot);
                            displayShotMarkers(selectedHole);
                            if (firebaseTeeBoxPath.contains("mens")) {
                                holePar.setText(dataSnapshot.child("mens par").getValue().toString());
                            }
                            else {
                                holePar.setText(dataSnapshot.child("ladies par").getValue().toString());
                            }

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

        holePutts = (TextView) view.findViewById(R.id.putts_number);

        addPutt = (ImageView) view.findViewById(R.id.putt_plus);
        addPutt.setVisibility(View.INVISIBLE);

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
                    .draggable(false)
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
                    .draggable(false)
                    .visible(false);
            marker = myMap.addMarker(ladiesTeeBoxMarker);
            holeMarkers.add(marker);
        }

        frontLocation = new Location("");
        frontLocation.setLatitude(Double.parseDouble(dataSnapshot.child("front green").child("latitude").getValue().toString()));
        frontLocation.setLongitude(Double.parseDouble(dataSnapshot.child("front green").child("longitude").getValue().toString()));
        // Front of Green Marker
        LatLng frontOfGreen = new LatLng(
                frontLocation.getLatitude(),
                frontLocation.getLongitude()
        );
        if (!frontOfGreen.equals(courseLatLng)) {
            mapBoundsBuilder.include(frontOfGreen);
            locationsToShow = true;
            MarkerOptions frontOfGreenMarker = new MarkerOptions()
                    .position(frontOfGreen)
                    .title(selectedHole + ". Front of Green")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .draggable(false)
                    .visible(false);
            marker = myMap.addMarker(frontOfGreenMarker);
            holeMarkers.add(marker);
        }

        middleLocation = new Location("");
        middleLocation.setLatitude(Double.parseDouble(dataSnapshot.child("middle green").child("latitude").getValue().toString()));
        middleLocation.setLongitude(Double.parseDouble(dataSnapshot.child("middle green").child("longitude").getValue().toString()));
        // Middle of Green Marker
        LatLng middleOfGreen = new LatLng(
                middleLocation.getLatitude(),
                middleLocation.getLongitude()
        );
        if (!middleOfGreen.equals(courseLatLng)) {
            mapBoundsBuilder.include(middleOfGreen);
            locationsToShow = true;
            MarkerOptions middleOfGreenMarker = new MarkerOptions()
                    .position(middleOfGreen)
                    .title(selectedHole + ". Middle of Green")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .draggable(false)
                    .visible(false);
            marker = myMap.addMarker(middleOfGreenMarker);
            holeMarkers.add(marker);
        }

        backLocation = new Location("");
        backLocation.setLatitude(Double.parseDouble(dataSnapshot.child("back green").child("latitude").getValue().toString()));
        backLocation.setLongitude(Double.parseDouble(dataSnapshot.child("back green").child("longitude").getValue().toString()));
        // Back of Green Marker
        LatLng backOfGreen = new LatLng(
                backLocation.getLatitude(),
                backLocation.getLongitude()
        );
        if (!backOfGreen.equals(courseLatLng)) {
            mapBoundsBuilder.include(backOfGreen);
            locationsToShow = true;
            MarkerOptions backOfGreenMarker = new MarkerOptions()
                    .position(backOfGreen)
                    .title(selectedHole + ". Back of Green")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .draggable(false)
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

    public void displayShotMarkers(final String selectedHole) {
        roundsRef.child("holes").child(selectedHole).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("putts")) {
                    holePutts.setText(dataSnapshot.child("putts").getChildrenCount() + "");
                    holePuttsInt = (int) dataSnapshot.child("putts").getChildrenCount();
                }
                else {
                    holePutts.setText("0");
                    holePuttsInt = 0;
                }

                int totalShots = (int) dataSnapshot.child("shots").getChildrenCount() + holePuttsInt;
                holeScore.setText(totalShots + "");
                if (totalShots > 0) {
                    Marker marker;
                    for (DataSnapshot data : dataSnapshot.child("shots").getChildren()) {
                        LatLng shotLocation = new LatLng(
                                Double.parseDouble(data.child("location").child("latitude").getValue().toString()),
                                Double.parseDouble(data.child("location").child("longitude").getValue().toString())
                        );
                        String ordinalIndicator = getOrdinalIndicator(Integer.parseInt(data.getKey()));

                        MarkerOptions shotMarker = new MarkerOptions()
                                .position(shotLocation)
                                .title(selectedHole + ". " + data.getKey() + ordinalIndicator + " Shot")
                                .icon(BitmapDescriptorFactory.fromBitmap(shotIcon));
                        marker = myMap.addMarker(shotMarker);
                        shotMarkers.add(marker);
                    }

                    if (holePuttsInt > 0) {
                        for (DataSnapshot data : dataSnapshot.child("putts").getChildren()) {
                            LatLng puttLocation = new LatLng(
                                    Double.parseDouble(data.child("location").child("latitude").getValue().toString()),
                                    Double.parseDouble(data.child("location").child("longitude").getValue().toString())
                            );
                            String ordinalIndicator = getOrdinalIndicator(Integer.parseInt(data.getKey()));

                            MarkerOptions puttMarker = new MarkerOptions()
                                    .position(puttLocation)
                                    .title(selectedHole + ". " + data.getKey() + ordinalIndicator + " Shot")
                                    .icon(BitmapDescriptorFactory.fromBitmap(puttIcon));
                            marker = myMap.addMarker(puttMarker);
                            shotMarkers.add(marker);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        myMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(getContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                info.addView(title);

                return info;
            }
        });
    }

    public String getOrdinalIndicator(int number) {
        if (number == 1) {
            return "st";
        }
        else if (number == 2) {
            return "nd";
        }
        else if (number == 3) {
            return "rd";
        }
        else  {
            return "th";
        }
    }
}
