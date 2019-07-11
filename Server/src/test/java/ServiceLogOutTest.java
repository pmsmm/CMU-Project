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

public class ServiceLogOutTest {

    private static final int OK = 200;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_LOGOUT = URL_BASE + "/logout";

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
    }

    @Test
    public void logOutValidSessionTest() {
        try {
            User user = new User("username", "password");
            operations.addUser(user);
            Session session = new Session("username", Operations.SESSION_DURATION);
            operations.addSession(session);

            String sessionId = String.valueOf(session.getSessionId());

            Request request = new Request.Builder().url(URL_LOGOUT + "/" + sessionId).delete().build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Session successfully deleted", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, user.getSessionId());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void logOutNonExistingSessionTest() {
        try {
            User user = new User("username", "password");
            operations.addUser(user);
            Session session = new Session("username", Operations.SESSION_DURATION);
            operations.addSession(session);

            Request request = new Request.Builder().url(URL_LOGOUT + "/1").delete().build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Session does not exist", mapResponse.get("error"));
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
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