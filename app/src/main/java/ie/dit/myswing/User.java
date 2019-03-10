package ie.dit.myswing;

public class User {
    private String firebaseKey;
    private String firstName;
    private String lastName;
    private String dob;
    private String club;
    private String society;
    private String gender;

    public User(String firebaseKey, String firstName, String lastName, String dob, String club, String society, String gender) {
        this.firebaseKey = firebaseKey;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.club = club;
        this.society = society;
        this.gender = gender;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDOB() {
        return dob;
    }

    public String getClub() {
        return club;
    }

    public String getSociety() {
        return society;
    }

    public String getGender() {
        return gender;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDOB(String dob) {
        this.dob = dob;
    }

    public void setClub(String club) {
        this.club = club;
    }

    public void setSociety(String society) {
        this.society = society;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
