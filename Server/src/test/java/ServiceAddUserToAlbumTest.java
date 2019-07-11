import com.google.gson.Gson;
import com.squareup.okhttp.*;
import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class ServiceAddUserToAlbumTest {

    private static final int CREATED = 201;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_ADD_USER = URL_BASE + "/adduser";

    Session session;

    @Before
    public void setUp() {
        original = new File(Operations.STATE_BACKUP_PATH);
        temporary = new File(Operations.STATE_BACKUP_NAME);
        //If there is already a backup file, it will be moved to other directory
        if (original.exists() && !original.isDirectory()) {
            try {
                FileUtils.moveFile(original, temporary);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        JavalinApp javalinApp = new JavalinApp();
        this.app = javalinApp.init();
        this.client = new OkHttpClient();
        operations = Operations.getServer();

        operations.addUser(new User("username", "password"));
        session = new Session("username", Operations.SESSION_DURATION);
        operations.addSession(session);
        operations.addAlbum(new Album("album", 10), "username");
        operations.setSliceURL(session.getSessionId(), "username", "http://www.url.com", 10);
    }

    @Test
    public void successfulAddUserToAlbum() {
        try {
            operations.addUser(new User("username2", "password"));

            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("sessionId", String.valueOf(session.getSessionId()));
            mapRequest.put("username", "username");
            mapRequest.put("albumId", "10");
            mapRequest.put("usernameToAdd", "username2");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_ADD_USER).put(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("User successfully added to album", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));

            Assert.assertEquals(2, operations.getAlbumById(10).getAlbumUserNumber());
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username2"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        session = null;

        this.app.stop();
        Operations.cleanServer(); //Also deletes server backup file
        operations = null;
        //If there was already a backup file, it is moved back to the backup directory
        if (temporary.exists() && !temporary.isDirectory()) {
            try {
                FileUtils.moveFile(temporary, original);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
