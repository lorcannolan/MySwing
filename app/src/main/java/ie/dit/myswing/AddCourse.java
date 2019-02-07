package ie.dit.myswing;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class AddCourse extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private PlacesClient placesClient;
    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference().child("courses");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setTitle("Add New Course");

        // Initialize Places, required as part of new static Places library rather than avilable through Play Servises
        // https://developers.google.com/places/android-sdk/client-migration
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBIo4_j-nnKuLtcqe4twifKoz-ZNcpO9gU");
        }
        // Create a new Places client instance.
        placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment
        // Info on autocomplete widget found here: https://developers.google.com/places/android-sdk/autocomplete
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Data fields to be returned from selection are outlined here
        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL
        ));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                setCamera(place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(AddCourse.this, "An error occurred: " + status, Toast.LENGTH_LONG).show();
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
