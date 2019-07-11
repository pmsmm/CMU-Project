import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class Operations {

    protected static final int MIN_USERNAME_LENGTH = 3;
    protected static final int MAX_USERNAME_LENGTH = 25;
    protected static final int MIN_PASSWORD_LENGTH = 8;
    protected static final int MAX_PASSWORD_LENGTH = 30;

    protected static final int SESSION_DURATION = 1000 * 60 * 60 * 4; //Milliseconds

    protected static final String TEMPORARY_BACKUP_NAME = "backups/ServerState.new";
    protected static final String STATE_BACKUP_NAME = "ServerState.old";
    protected static final String STATE_BACKUP_PATH = "backups/" + STATE_BACKUP_NAME;

    protected static final String SIGNUP_OPERATION = "SIGNUP";
    protected static final String LOGIN_OPERATION = "LOGIN";
    protected static final String LOGOUT_OPERATION = "LOGOUT";
    protected static final String LOGS_OPERATION = "LOGS";
    protected static final String CREATE_ALBUM_OPERATION = "CREATE_ALBUM";
    protected static final String SET_SLICE_URL_OPERATION = "SET_SLICE_URL";
    protected static final String GET_USERS_OPERATION = "GET_USERS";
    protected static final String ADD_USER_TO_ALBUM_OPERATION = "ADD_USER_TO_ALBUM";
    protected static final String GET_USER_ALBUMS_OPERATION = "GET_USER_ALBUMS";
    protected static final String VIEW_ALBUM = "VIEW_ALBUM";

    private static Operations operations;

    // Tells to the secondary thread if it must terminate or if it can continue running
    private static AtomicBoolean shutdown = new AtomicBoolean(false);

    private Map<Integer, Album> albums = new ConcurrentHashMap<>();
    //Username as KEY and USER value
    private Map<String, User> users = new ConcurrentHashMap<>();
    //Session ID as Key, SESSION object as value
    private Map<Integer, Session> sessions = new ConcurrentHashMap<>();
    private String logs = "";

    protected AtomicInteger counterAlbum = new AtomicInteger(0);
    protected AtomicInteger counterLog = new AtomicInteger(0);

    private Operations() {

    }

    // Singleton

    public static Operations getServer() {
        if (Operations.operations != null)
            return Operations.operations;
        Operations.operations = new Operations();
        Operations.readServerState();

        // Create second thread to search for expired sessions and clean them
        Runnable r = Operations.operations::cleanSessions;
        new Thread(r).start();

        return Operations.operations;
    }

    // Business logic main methods

    public HashMap<String, String> signUp(String username, String password) {
        HashMap<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        HashMap<String, String> response = new HashMap<>();

        String usernameEvaluation = isUsernameValid(username);
        if(!usernameEvaluation.equals("Username valid")) {
            response.put("error", usernameEvaluation);
            addLog(SIGNUP_OPERATION, request, response);
            return response;
        }
        String passwordEvaluation = isPasswordValid(password);
        if(!passwordEvaluation.equals("Password valid")) {
            response.put("error", passwordEvaluation);
            addLog(SIGNUP_OPERATION, request, response);
            return response;
        }
        addUser(new User(username, password));
        response.put("success", "User created successfully");
        addLog(SIGNUP_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> logIn(String username, String password){
        HashMap<String, String> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        HashMap<String, String> response = new HashMap<>();

        if(!isUserCreated(username)){
            response.put("error", "The Inserted Username is Incorrect!");
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        if(!getUserByUsername(username).isPasswordCorrect(password)){
            response.put("error", "Invalid Password! Please Try Again");
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        if(getUserByUsername(username).getSessionId() > 0) {
            response.put("success", "Login successful");
            response.put("sessionId", String.valueOf(getUserByUsername(username).getSessionId()));
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        Session session = new Session(username, SESSION_DURATION);
        String sessionAddResult = addSession(session);
        if(!sessionAddResult.equals("Session successfully added")) {
            response.put("error", "Could not create session - Please try to log in again");
            addLog(LOGIN_OPERATION, request, response);
            return response;
        }
        response.put("success", "Login successful");
        response.put("sessionId", String.valueOf(session.getSessionId()));
        addLog(LOGIN_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> logOut(int sessionId) {
        HashMap<String, String> request = new HashMap<>();
        request.put("sessionId", String.valueOf(sessionId));
        HashMap<String, String> response = new HashMap<>();
        String result = deleteSession(sessionId);
        if(!result.equals("Session successfully deleted")) {
            response.put("error", result);
            addLog(LOGOUT_OPERATION, request, response);
            return response;
        }
        response.put("success", result);
        addLog(LOGOUT_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> serviceGetLogs() {
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> response = new HashMap<>();
        response.put("success", "Logs correctly obtained");
        addLog(LOGS_OPERATION, request, response);
        response.put("logs", getLogs());
        return response;
    }

    public HashMap<String, String> createAlbum(int sessionId, String username, String albumName) {
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> response = new HashMap<>();
        request.put("sessionId", String.valueOf(sessionId));
        request.put("username", username);
        request.put("albumName", albumName);
        if(!sessionVerifier(username, sessionId).equals("Valid session")) {
            response.put("error", sessionVerifier(username, sessionId));
            addLog(CREATE_ALBUM_OPERATION, request, response);
            return response;
        }
        String[] result = addAlbum(new Album(albumName, counterAlbum.incrementAndGet()), username);
        if(!result[0].equals("Album successfully added")) {
            response.put("error", result[0]);
            addLog(CREATE_ALBUM_OPERATION, request, response);
            return response;
        }
        response.put("success", result[0]);
        response.put("albumId", result[1]);
        addLog(CREATE_ALBUM_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> setSliceURL(int sessionId, String username, String URL, int albumId) {
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> response = new HashMap<>();
        request.put("sessionId", String.valueOf(sessionId));
        request.put("username", username);
        request.put("URL", URL);
        request.put("albumId", String.valueOf(albumId));
        if(!sessionVerifier(username, sessionId).equals("Valid session")) {
            response.put("error", sessionVerifier(username, sessionId));
            addLog(SET_SLICE_URL_OPERATION, request, response);
            return response;
        }
        String result = addSliceURLtoAlbum(albumId, URL, username);
        if(!result.equals("URL successfully set")) {
            response.put("error", result);
            addLog(SET_SLICE_URL_OPERATION, request, response);
            return response;
        }
        response.put("success", result);
        addLog(SET_SLICE_URL_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> serviceGetUsers(int sessionId, String username) {
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> response = new HashMap<>();
        request.put("sessionId", String.valueOf(sessionId));
        request.put("username", username);
        if(!sessionVerifier(username, sessionId).equals("Valid session")) {
            response.put("error", sessionVerifier(username, sessionId));
            addLog(GET_USERS_OPERATION, request, response);
            return response;
        }
        response = getUsersHashMap();
        response.put("success", "Users successfully obtained");
        addLog(GET_USERS_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> serviceAddUserToAlbum(int sessionId, String requestingUsername, int albumId, String usernameToAdd) {
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> response = new HashMap<>();
        request.put("sessionId", String.valueOf(sessionId));
        request.put("username", requestingUsername);
        request.put("albumId", String.valueOf(albumId));
        request.put("usernameToAdd", usernameToAdd);
        if(!sessionVerifier(requestingUsername, sessionId).equals("Valid session")) {
            response.put("error", sessionVerifier(requestingUsername, sessionId));
            addLog(ADD_USER_TO_ALBUM_OPERATION, request, response);
            return response;
        }
        String result = addUserToAlbum(albumId, requestingUsername, usernameToAdd);
        if(!result.equals("User successfully added to album")) {
            response.put("error", result);
            addLog(ADD_USER_TO_ALBUM_OPERATION, request, response);
            return response;
        }
        response.put("success", result);
        addLog(ADD_USER_TO_ALBUM_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> serviceGetUserAlbums(int sessionId, String username) {
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> response = new HashMap<>();
        request.put("sessionId", String.valueOf(sessionId));
        request.put("username", username);
        if(!sessionVerifier(username, sessionId).equals("Valid session")) {
            response.put("error", sessionVerifier(username, sessionId));
            addLog(GET_USER_ALBUMS_OPERATION, request, response);
            return response;
        }
        response = getUserAlbumsHashMap(username);
        if(response==null) {
            response = new HashMap<>();
            response.put("error", "Invalid username");
            addLog(GET_USER_ALBUMS_OPERATION, request, response);
            return response;
        }
        response.put("success", "User albums successfully obtained");
        addLog(GET_USER_ALBUMS_OPERATION, request, response);
        return response;
    }

    public HashMap<String, String> viewAlbum(int sessionId, String username, int albumId) {
        HashMap<String, String> request = new HashMap<>();
        HashMap<String, String> response = new HashMap<>();
        request.put("sessionId", String.valueOf(sessionId));
        request.put("username", username);
        if(!sessionVerifier(username, sessionId).equals("Valid session")) {
            response.put("error", sessionVerifier(username, sessionId));
            addLog(VIEW_ALBUM, request, response);
            return response;
        }
        response = getAlbumHashMap(username, albumId);
        if(response==null) {
            response = new HashMap<>();
            response.put("error", "Invalid username or album id");
            addLog(VIEW_ALBUM, request, response);
            return response;
        }
        response.put("success", "Album successfully obtained");
        addLog(VIEW_ALBUM, request, response);
        return response;
    }

    // Business logic auxiliary methods

    protected String isUsernameValid(String username) {
        if (username == null)
            return "Username cannot be null";
        if (username.length() == 0 || username.trim().length() == 0)
            return "Username cannot be empty";
        for (int i=0; i<username.length(); i++)
            if(!Character.isDigit(username.charAt(i)) && !Character.isLetter(username.charAt(i)))
                return "Username must only contain digits and letters";
        if (username.length() < MIN_USERNAME_LENGTH)
            return "Username must have at least " + MIN_USERNAME_LENGTH + " characters";
        if (username.length() > MAX_USERNAME_LENGTH)
            return "Username must have at most " + MAX_USERNAME_LENGTH + " characters";
        //username = username.toLowerCase();
        if (isUserCreated(username))
            return "Username already exists";
        return "Username valid";
    }

    protected String isPasswordValid(String password) {
        if (password == null)
            return "Password cannot be null";
        if (password.length() == 0 || password.trim().length() == 0)
            return "Password cannot be empty";
        if (password.length() < MIN_PASSWORD_LENGTH)
            return "Password must have at least " + MIN_PASSWORD_LENGTH + " characters";
        if (password.length() > MAX_PASSWORD_LENGTH)
            return "Password must have at most " + MAX_PASSWORD_LENGTH + " characters";
        return "Password valid";
    }

    protected String sessionVerifier(String username, int sessionId) {
        if(!isUserCreated(username))
            return "Invalid username";
        if(sessionId<=0) //A session id of 0 in a user means it has no session, 0 is not a valid session id
            return "Invalid session id";
        User user = getUserByUsername(username);
        if(user.getSessionId() == sessionId)
            return "Valid session";
        return "Invalid session id";
    }

    protected HashMap<String, String> getUsersHashMap() {
        HashMap<String, String> response = new HashMap<>();
        String username;
        String users = "";
        Iterator iterator = getUsers().entrySet().iterator();
        Map.Entry pair;
        while (iterator.hasNext()) {
            pair = (Map.Entry) iterator.next();
            username = (String) pair.getKey();
            users+=username;
            if(iterator.hasNext())
                users+=",";
        }
        response.put("size", String.valueOf(getUsersLength()));
        response.put("users", users);
        return response;
    }

    protected HashMap<String, String> getUserAlbumsHashMap(String username) {
        if(!isUserCreated(username))
            return null;
        HashMap<String, String> response = new HashMap<>();
        int albumId;
        String albums = "";
        Iterator iterator = getUserByUsername(username).getAlbums().iterator();
        while (iterator.hasNext()) {
            albumId = (Integer) iterator.next();
            albums+=albumId;
            if(iterator.hasNext())
                albums+=",";
            response.put(String.valueOf(albumId), getAlbumById(albumId).getName());
            response.put("Users_"+albumId, getAlbumHashMap(username, albumId).get("users"));
        }
        response.put("size", String.valueOf(getUserByUsername(username).getUserAlbumNumber()));
        response.put("albums", albums);
        return response;
    }

    protected HashMap<String, String> getAlbumHashMap(String username, int albumId) {
        if(!isUserCreated(username))
            return null;
        Album album = getAlbumById(albumId);
        if(album==null)
            return null;
        if(!album.isUserInAlbum(username))
            return null;
        HashMap<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(album.getId()));
        response.put("name", album.getName());
        response.put("size", String.valueOf(album.getAlbumUserNumber()));

        String sliceURL;
        String users = "";
        Map.Entry pair;
        Iterator iterator = album.getSlices().entrySet().iterator();
        while (iterator.hasNext()) {
            pair = (Map.Entry) iterator.next();
            username = (String) pair.getKey();
            sliceURL = (String) pair.getValue();
            users+=username;
            if(iterator.hasNext())
                users+=",";
            response.put(username, sliceURL);
        }
        response.put("users", users);
        return response;
    }

    // Server state setters

    protected String[] addAlbum(Album album, String username) {
        String[] response = new String[2];
        if (album != null) {
            if (!(album.getName()==null || album.getName().trim().length()==0)) {
                if (isUserCreated(username)) {
                    album.addUserToAlbum(username, null);
                    getUserByUsername(username).addAlbumUserIsIn(album.getId());
                    albums.put(album.getId(), album);
                    Operations.writeServerState();
                    response[0] = "Album successfully added";
                    response[1] = String.valueOf(album.getId());
                    return response;
                }
                response[0] = "Username does not exist or is invalid";
                return response;
            }
            response[0] = "Album name cannot be empty or null";
            return response;
        }
        response[0] = "Album cannot be null";
        return response;
    }

    protected String addSliceURLtoAlbum(int albumId, String URL, String username) {
        Album album = getAlbumById(albumId);
        if(album!=null) {
            if(album.isUserInAlbum(username)) {
                if(URL!=null && URL.trim().length()!=0) {
                    album.addUserToAlbum(username, URL); //User is already in album, so URL will be overwritten
                    Operations.writeServerState();
                    return "URL successfully set";
                }
                return "URL must not be null or empty";
            }
            return "User does not belong to the album";
        }
        return "Album id is invalid or does not exist";
    }

    protected String addUserToAlbum(int albumId, String requestUsername, String usernameToAdd) {
        Album album = getAlbumById(albumId);
        if(album!=null) {
            if(album.isUserInAlbum(requestUsername)) {
                if(!album.isUserInAlbum(usernameToAdd)) {
                    if(isUserCreated(usernameToAdd)) {
                        album.addUserToAlbum(usernameToAdd, null);
                        getUserByUsername(usernameToAdd).addAlbumUserIsIn(album.getId());
                        Operations.writeServerState();
                        return "User successfully added to album";
                    }
                    return "User not found";
                }
                return "User is already in the album";
            }
            return "Requesting user does not belong to the album";
        }
        return "Album id is invalid or does not exist";
    }

    protected String addUser(User user) {
        if (user != null) {
            if (!isUserCreated(user.getUsername())) {
                users.put(user.getUsername(), user);
                Operations.writeServerState();
                return "User successfully added";
            }
            return "User already exists";
        }
        return "User cannot be null";
    }

    protected String addSession(Session session) {
        if (session != null) {
            if (!isSessionCreated(session.getSessionId())) {
                if(isUserCreated(session.getUsername())) {
                    if (getUserByUsername(session.getUsername()).getSessionId() == 0) {
                        sessions.put(session.getSessionId(), session);
                        getUserByUsername(session.getUsername()).setSessionId(session.getSessionId());
                        Operations.writeServerState();
                        return "Session successfully added";
                    }
                    return "User already has a session";
                }
                return "User does not exist";
            }
            return "Session already exists";
        }
        return "Session cannot be null";
    }

    protected String addLog(String operation, HashMap request, HashMap response) {
        if(operation!=null) {
            if(request!=null) {
                if(response!=null) {
                    String log = "";
                    log+="Operation ID: " + counterLog.incrementAndGet() + "\n";
                    log+="Operation name: " + operation + "\n";
                    log+="Operation time: " + new Date().toString() + "\n";
                    log+="Operation input: " + new Gson().toJson(request) + "\n";
                    log+="Operation output: " + new Gson().toJson(response) + "\n";
                    log+="---------------------------------------------------------------------------------------------------------------\n";
                    logs = log + logs;
                    Operations.writeServerState();
                    System.out.println(log);
                    return "Operation successfully logged";
                }
                return "Operation response cannot be null";
            }
            return "Operation request cannot be null";
        }
        return "Operation name cannot be null";
    }

    protected synchronized String deleteSession(int sessionId) {
        if(isSessionCreated(sessionId)) {
            getUserByUsername(getSessionById(sessionId).getUsername()).setSessionId(0);
            sessions.remove(sessionId);
            Operations.writeServerState();
            return "Session successfully deleted";
        }
        return "Session does not exist";
    }

    // Server state getters

    protected HashMap<Integer, Album> getAlbums() {
        return new HashMap<>(albums);
    }

    protected Album getAlbumById(int albumId) {
        if (isAlbumCreated(albumId))
            return albums.get(albumId);
        return null;
    }

    private boolean isAlbumCreated(int albumId) {
        return albums.containsKey(albumId);
    }

    protected int getAlbumsLength() {
        return albums.size();
    }

    protected HashMap<Integer, Session> getSessions() {
        return new HashMap<>(sessions);
    }

    protected Session getSessionById(int sessionId) {
        if (isSessionCreated(sessionId))
            return sessions.get(sessionId);
        return null;
    }

    private boolean isSessionCreated(int sessionId) {
        return sessions.containsKey(sessionId);
    }

    protected int getSessionsLength() {
        return sessions.size();
    }

    protected HashMap<String, User> getUsers() {
        return new HashMap<>(users);
    }

    protected User getUserByUsername(String username) {
        if (isUserCreated(username))
            return users.get(username);
        return null;
    }

    private boolean isUserCreated(String username) {
        if(username==null)
            return false;
        return users.containsKey(username);
    }

    protected int getUsersLength() {
        return users.size();
    }

    protected String getLogs() {
        return logs;
    }

    protected int getLogsLength() {
        return counterLog.get();
    }

    // Server state backup methods

    private static void readServerState() {
        if (isBackupFileCreatedAndNotEmpty()) {
            try {
                Gson gson = new Gson();
                String jsonString = FileUtils.readFileToString(new File(STATE_BACKUP_PATH), "UTF-8");
                jsonString = jsonString.replace("\n", "").replace("\r", "");
                Operations.operations = gson.fromJson(jsonString, Operations.class);
                System.out.println("Recovered Server State");
            } catch (Exception e) {
                System.out.println("Backup file found is unreadable - Creating a new one");
                Operations.writeServerState();
            }
        } else {
            System.out.println("No written backup file found - Creating a new one");
            Operations.writeServerState();
        }
    }

    private static boolean isBackupFileCreatedAndNotEmpty() {
        File f = new File(STATE_BACKUP_PATH);
        if (f.exists() && !f.isDirectory()) {
            try {
                String jsonString = FileUtils.readFileToString(new File(STATE_BACKUP_PATH), "UTF-8");
                jsonString = jsonString.replace("\n", "").replace("\r", "");
                jsonString = jsonString.trim();
                if (jsonString.length() > 0)
                    return true;
                System.out.println("Backup file is empty");
                return false;
            } catch (Exception e) {
                System.out.println("Failed to assert if backup file is not empty");
                return false;
            }
        }
        System.out.println("Backup file not found");
        return false;
    }

    private synchronized static void writeServerState() {
        try {
            new File(Operations.TEMPORARY_BACKUP_NAME);
            PrintWriter writer = new PrintWriter(Operations.TEMPORARY_BACKUP_NAME);
            writer.println(new Gson().toJson(Operations.getServer()));
            writer.close();
            //System.out.println(new Gson().toJson(Operations.getServer()));
            Files.move(Paths.get(TEMPORARY_BACKUP_NAME), Paths.get(STATE_BACKUP_PATH), ATOMIC_MOVE);
            System.out.println("Backup file created");
        } catch (Exception e) {
            System.out.println("Could not backup server state");
        }
    }

    // Server cleaner

    protected static void cleanServer() {
        try {
            Operations.shutdown.set(true);
            while(Operations.shutdown.get()) {} //Waits for session cleaner thread to stop
            Operations.operations = null;
            File backup = new File(Operations.STATE_BACKUP_PATH);
            // Deletes backup file
            if (backup.exists() && !backup.isDirectory()) {
                backup.delete();
                System.out.println("Server backup file deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to be run in a separate thread to delete expired sessions

    protected void cleanSessions() {
        try {
            Session toCheck;
            Iterator iterator;
            Map.Entry pair;
            while (!Operations.shutdown.get()) {
                Thread.sleep(100);
                iterator = new HashMap<>(sessions).entrySet().iterator();
                while (iterator.hasNext()) {
                    pair = (Map.Entry) iterator.next();
                    toCheck = (Session) pair.getValue();
                    if (!toCheck.isSessionValid()) {
                        deleteSession(toCheck.getSessionId());
                        System.out.println("Session " + toCheck.getSessionId() + " belonging to user " + toCheck.getUsername() + " expired and was cleaned");
                    }
                }
            }
            Operations.shutdown.set(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
