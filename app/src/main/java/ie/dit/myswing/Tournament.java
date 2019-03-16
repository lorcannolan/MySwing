package ie.dit.myswing;

public class Tournament {
    private String firebaseKey;
    private String name;
    private String courseID;
    private String date;
    private String club;
    private String society;
    private String[] invited;
    private String courseName;

    public Tournament() {}

    public Tournament(String firebaseKey, String name, String courseID, String date) {
        this.firebaseKey = firebaseKey;
        this.name = name;
        this.courseID = courseID;
        this.date = date;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public String getName() {
        return name;
    }

    public String getCourseID() {
        return courseID;
    }

    public String getDate() {
        return date;
    }

    public String getClub() {
        return club;
    }

    public String getSociety() {
        return society;
    }

    public String[] getInvited() {
        return invited;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setClub(String club) {
        this.club = club;
    }

    public void setSociety(String society) {
        this.society = society;
    }

    public void setInvited(String[] invited) {
        this.invited = invited;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
