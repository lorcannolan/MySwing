package ie.dit.myswing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ConfigureMapFragment extends Fragment implements OnMapReadyCallback, ConfigureCourse.OnLatLangReceivedListener {

    private GoogleMap myMap;

    private String latitude;
    private String longitude;
    private LatLng latLng;

    private FloatingTextButton zoom;

    private static final String TAG = "ConfigureMapFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_configure_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.add_course_locations_map);
        mapFragment.getMapAsync(this);

        ConfigureCourse configureCourseAcitvity = (ConfigureCourse) getActivity();
        configureCourseAcitvity.setLatLngListener(this);

        zoom = (FloatingTextButton)view.findViewById(R.id.focus);
        zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveCam(latLng);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (latLng != null) {
            moveCam(latLng);
        }
    }

    @Override
    public void onLatLngReceived(String latitude, String longitude) {
        latLng = new LatLng(
                Double.parseDouble(latitude),
                Double.parseDouble(longitude)
        );
    }

    public void moveCam(LatLng latLng) {
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        MarkerOptions courseLocation = new MarkerOptions()
                .position(latLng);
        myMap.addMarker(courseLocation);
    }
}
