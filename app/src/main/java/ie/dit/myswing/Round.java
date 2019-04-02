package ie.dit.myswing;

public class Round {

    private String firebaseRoundID;
    private String courseName;
    private String courseID;
    private String date;
    private int handicap;
    private int score;
    private int totalPutts;

    public Round (String firebaseRoundID, String courseName, String courseID, String date, int handicap, int score, int totalPutts) {
        this.firebaseRoundID = firebaseRoundID;
        this.courseName = courseName;
        this.courseID = courseID;
        this.date = date;
        this.handicap = handicap;
        this.score = score;
        this.totalPutts = totalPutts;
    }

    public String getFirebaseRoundID() {
        return firebaseRoundID;
    }

    public void setFirebaseRoundID(String firebaseRoundID) {
        this.firebaseRoundID = firebaseRoundID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID(String courseID) {
        this.courseID = courseID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getHandicap() {
        return handicap;
    }

    public void setHandicap(int handicap) {
        this.handicap = handicap;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalPutts() {
        return totalPutts;
    }

    public void setTotalPutts(int totalPutts) {
        this.totalPutts = totalPutts;
    }
}
