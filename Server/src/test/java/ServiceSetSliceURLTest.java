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

public class ServiceSetSliceURLTest {

    private static final int CREATED = 201;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_SET_URL = URL_BASE + "/seturl";

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
        operations.addAlbum(new Album("album", 1), "username");
    }

    @Test
    public void successSetSliceURLTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("sessionId", String.valueOf(session.getSessionId()));
            mapRequest.put("username", "username");
            mapRequest.put("URL", "http://www.url.com");
            mapRequest.put("albumId", "1");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_SET_URL).put(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("URL successfully set", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));

            Assert.assertEquals("http://www.url.com", operations.getAlbumById(1).getSliceURL("username"));
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
