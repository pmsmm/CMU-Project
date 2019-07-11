import java.util.ArrayList;

public class User {

    private String username; //Will serve as ID
    private String password;
    private ArrayList<Integer> albums = new ArrayList<>();
    private int sessionId;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Integer> getAlbums() {
        return albums;
    }

    public void setAlbums(ArrayList<Integer> albums) {
        this.albums = albums;
    }

    public boolean isUserInAlbum(int albumId) {
        return albums.contains(albumId);
    }

    public int getUserAlbumNumber() {
        return albums.size();
    }

    public void addAlbumUserIsIn(int albumId) {
        albums.add(albumId);
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isPasswordCorrect(String password) {
        if(password == null)
            return false;
        return this.password.equals(password);
    }
}
