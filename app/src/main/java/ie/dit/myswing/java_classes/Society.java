package ie.dit.myswing.java_classes;

public class Society {
    private String firebaseKey;
    private String name;
    private String organization;
    private String createdBy;

    public Society (String firebaseKey, String name, String organization, String createdBy) {
        this.firebaseKey = firebaseKey;
        this.name = name;
        this.organization = organization;
        this.createdBy = createdBy;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public String getName() {
        return name;
    }

    public String getOrganization() {
        return organization;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
