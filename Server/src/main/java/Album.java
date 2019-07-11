import java.util.HashMap;

public class Album {

    private int id;
    // Maps username to slice URL
    private HashMap<String, String> slices;
    private String name;

    public Album(String name, int id) {
        this.slices = new HashMap<>();
        this.name = name;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<String, String> getSlices() {
        return slices;
    }

    public void setSlices(HashMap<String, String> albumMap) {
        this.slices = albumMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUserInAlbum(String username) {
        return slices.containsKey(username);
    }

    public String getSliceURL(String username) {
        if (isUserInAlbum(username))
            return slices.get(username);
        return "User is not in album";
    }

    public int getAlbumUserNumber() {
        return slices.size();
    }

    public void addUserToAlbum(String username, String sliceURL) {
        if(username!=null && username.trim().length()>0)
            slices.put(username, sliceURL);
    }
}
