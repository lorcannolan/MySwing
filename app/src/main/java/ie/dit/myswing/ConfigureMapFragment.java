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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ConfigureMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap myMap;

    private LatLng courseLatLng;
    private DatabaseReference holesRef;

    private String courseFirebaseKey, courseName;
    private Spinner selectHoleSpinner;
    private RelativeLayout spinnerContainer;
    private String selectedHole;

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
        courseName = i.getStringExtra("courseName");
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

        courseLatLng = new LatLng(
                Double.parseDouble(courseLatitude),
                Double.parseDouble(courseLongitude)
        );

        return view;
    }

    public void displayMarkers(DataSnapshot dataSnapshot) {
        /*
            Information relating to LatLngBounds was found here:
                - https://stackoverflow.com/questions/15540220/google-map-camera-position-on-multiple-markers
            LatLngBounds enables camera to zoom to markers and display all markers within the bounds of the screen.
         */
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
                    .draggable(true);
            myMap.addMarker(mensTeeBoxMarker);
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
                    .draggable(true);
            myMap.addMarker(ladiesTeeBoxMarker);
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
                    .draggable(true);
            myMap.addMarker(frontOfGreenMarker);
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
                    .draggable(true);
            myMap.addMarker(middleOfGreenMarker);
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
                    .draggable(true);
            myMap.addMarker(backOfGreenMarker);
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

    // TODO:
    // Come back to this: https://stackoverflow.com/questions/14631334/android-maps-v2-newlatlngbounds-with-bearing
//    public int getBoundsZoomLevel(LatLngBounds bounds, int mapWidthPx, int mapHeightPx){
//
//        LatLng ne = bounds.northeast;
//        LatLng sw = bounds.southwest;
//
//        double latFraction = (latRad(ne.latitude) - latRad(sw.latitude)) / Math.PI;
//
//        double lngDiff = ne.longitude - sw.longitude;
//        double lngFraction = ((lngDiff < 0) ? (lngDiff + 360) : lngDiff) / 360;
//
//        double latZoom = zoom(mapHeightPx, WORLD_PX_HEIGHT, latFraction);
//        double lngZoom = zoom(mapWidthPx, WORLD_PX_WIDTH, lngFraction);
//
//        int result = Math.min((int)latZoom, (int)lngZoom);
//        return Math.min(result, ZOOM_MAX);
//    }
//
//    private double latRad(double lat) {
//        double sin = Math.sin(lat * Math.PI / 180);
//        double radX2 = Math.log((1 + sin) / (1 - sin)) / 2;
//        return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2;
//    }
//    private double zoom(int mapPx, int worldPx, double fraction) {
//        return Math.floor(Math.log(mapPx / worldPx / fraction) / LN2);
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        myMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                if (!selectedHole.equalsIgnoreCase("-select hole-")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Select the Location Type for this Marker.");
                    builder.setItems(R.array.location_type, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Resources res = getContext().getResources();
                            String[] locationType = res.getStringArray(R.array.location_type);
                            String chosenLocationType = (locationType[which]);
                            String firebasePath = "";
                            if (!selectedHole.equalsIgnoreCase("-select hole-")) {
                                switch (chosenLocationType) {
                                    case "Men's Tee Box":
                                        firebasePath = "mens tee box";
                                        break;
                                    case "Ladies Tee Box":
                                        firebasePath = "ladies tee box";
                                        break;
                                    case "Front of Green":
                                        firebasePath = "front green";
                                        break;
                                    case "Middle of Green":
                                        firebasePath = "middle green";
                                        break;
                                    case "Back of Green":
                                        firebasePath = "back green";
                                        break;
                                }
                                holesRef.child(selectedHole).child(firebasePath).setValue(latLng);
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
            }
        });

        myMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                if (!marker.getTitle().equalsIgnoreCase(courseName)) {
                    String firebasePath = "";
                    if (marker.getTitle().contains("Men's")) {
                        firebasePath = "mens tee box";
                    }
                    else if (marker.getTitle().contains("Ladies")) {
                        firebasePath = "ladies tee box";
                    }
                    else if (marker.getTitle().contains("Front")) {
                        firebasePath = "front green";
                    }
                    else if (marker.getTitle().contains("Middle")) {
                        firebasePath = "middle green";
                    }
                    else if (marker.getTitle().contains("Back")) {
                        firebasePath = "back green";
                    }
                    holesRef.child(selectedHole).child(firebasePath).child("latitude").setValue(marker.getPosition().latitude);
                    holesRef.child(selectedHole).child(firebasePath).child("longitude").setValue(marker.getPosition().longitude);
                }
            }
        });
    }
}
