import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class ServiceGetUsersTest {

    private static final int OK = 200;

    private static final int PORT = JavalinApp.PORT;

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_GET_USERS = URL_BASE + "/users";

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
    public void getUsersOneUserTest() {
        try {
            String URL = URL_GET_USERS + "/" + session.getSessionId() + "/username";
            Request request = new Request.Builder().url(URL).get().build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Users successfully obtained", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("1", mapResponse.get("size"));
            Assert.assertEquals("username", mapResponse.get("users"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getUsersTwoUsers() {
        try {
            operations.addUser(new User("username2", "password"));

            String URL = URL_GET_USERS + "/" + session.getSessionId() + "/username";
            Request request = new Request.Builder().url(URL).get().build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Users successfully obtained", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("2", mapResponse.get("size"));
            Assert.assertEquals("username2,username", mapResponse.get("users"));
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
