package ie.dit.myswing.map;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

import ie.dit.myswing.profile.JoinClub;
import ie.dit.myswing.R;
import ie.dit.myswing.greeting.Home;
import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class AddCourse extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private PlacesClient placesClient;
    private FloatingTextButton floatingTextButton;
    private Place currentPlace;

    private static final String TAG = "AddCourse";
    private boolean dataPresent = false;

    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        final CoordinatorLayout container = (CoordinatorLayout) findViewById(R.id.add_course_layout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setTitle("Select Course to Play");

        // Initialize Places, required as part of new static Places library rather than avilable through Play Servises
        // https://developers.google.com/places/android-sdk/client-migration
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBIo4_j-nnKuLtcqe4twifKoz-ZNcpO9gU");
        }
        // Create a new Places client instance.
        placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment
        // Info on autocomplete widget found here: https://developers.google.com/places/android-sdk/autocomplete
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.a.setHint("Search Course");

        // Data fields to be returned from selection are outlined here
        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.WEBSITE_URI,
                Place.Field.PHOTO_METADATAS
        ));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                setCamera(place);
                currentPlace = place;
                dataPresent = false;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(AddCourse.this, "An error occurred: " + status, Toast.LENGTH_LONG).show();
            }
        });

        floatingTextButton = (FloatingTextButton)findViewById(R.id.save);
        floatingTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
                //add location bias, hint, etc
                if (autocompleteFragment.a.getText().toString().equals("")) {
                    Snackbar.make(container, "Must Enter Text Value", Snackbar.LENGTH_LONG).show();
                }
                else {
                    courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                // If data exists in database, break and set boolean
                                if (data.child("placesID").getValue().toString().equals(currentPlace.getId())) {
                                    dataPresent = true;
                                    break;
                                }
                            }

                            //If data doesn't exist, save course information
                            if (!dataPresent) {
                                String id = courseRef.push().getKey();
                                courseRef.child(id).child("placesID").setValue(currentPlace.getId());
                                courseRef.child(id).child("name").setValue(currentPlace.getName());
                                courseRef.child(id).child("Address").setValue(currentPlace.getAddress());
                                courseRef.child(id).child("location").setValue(currentPlace.getLatLng());
                                courseRef.child(id).child("website").setValue(currentPlace.getWebsiteUri().toString());

                                // After adding new course, reload map fragment or JoinClub
                                Intent returnToSource;
                                if (getIntent().getStringExtra("source").equalsIgnoreCase("joinclub")) {
                                    returnToSource = new Intent(AddCourse.this, JoinClub.class);
                                    returnToSource.putExtra("source", "AddCourse");
                                    if (getIntent().hasExtra("parentSource")) {
                                        if (getIntent().getStringExtra("parentSource").equalsIgnoreCase("create tournament join club")) {
                                            returnToSource.putExtra("destination", "Create Tournament Join Club");
                                        }
                                        else if (getIntent().getStringExtra("parentSource").equalsIgnoreCase("create tournament select club")) {
                                            returnToSource.putExtra("destination", "Create Tournament Select Club");
                                        }
                                    }
                                }
                                else {
                                    returnToSource = new Intent(AddCourse.this, Home.class);
                                    returnToSource.putExtra("fragment", "Map");
                                }
                                startActivity(returnToSource);
                                finish();
                            } else {
                                Snackbar.make(container, "Data Already Exists in Database", Snackbar.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    public void setCamera(Place selected) {
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selected.getLatLng(), 15f));
        MarkerOptions courseLocation = new MarkerOptions()
                .position(selected.getLatLng())
                .title(selected.getName());
        myMap.addMarker(courseLocation);
    }
}
