package pt.ulisboa.tecnico.cmov.proj.Data;

public class User {

    private int userID = -1;
    private String userName = "User";

    public User(int userID, String userName) {
        this.userID = userID;
        this.userName = userName;
    }

    public int getUserID() { return this.userID; }
    public String getUserName() { return this.userName; }
}
