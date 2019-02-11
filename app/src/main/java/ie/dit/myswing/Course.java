package ie.dit.myswing;

import com.google.android.gms.maps.model.LatLng;

public class Course {
    private String placesID;
    private String name;
    private String address;
    private String websiteURI;
    private String latitude;
    private String longitude;

    public Course (String placesID, String name, String address, String websiteURI, String latitude, String longitude) {
        this.placesID = placesID;
        this.name = name;
        this.address = address;
        this.websiteURI = websiteURI;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlacesID() {
        return placesID;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getWebsiteURI() {
        return websiteURI;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setPlacesID(String placesID) {
        this.placesID = placesID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setWebsiteURI(String websiteURI) {
        this.websiteURI = websiteURI;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
