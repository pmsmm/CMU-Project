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

public class ServiceLogsTest {

    private static final int OK = 200;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_LOGS = URL_BASE + "/logs";
    private static final String URL_SIGNUP = URL_BASE + "/signup";
    private static final String URL_LOGIN = URL_BASE + "/login";

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
    public void serviceGetEmptyLogsTest() {
        try {
            Request request = new Request.Builder().url(URL_LOGS).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);

            String logs = mapResponse.get("logs");
            Assert.assertTrue(logs.contains("Operation ID: 1"));
            Assert.assertFalse(logs.contains("Operation ID: 2"));
            Assert.assertTrue(logs.contains("Operation name: LOGS"));
            Assert.assertTrue(logs.contains("Operation time:"));
            Assert.assertTrue(logs.contains("Operation input: {}"));
            Assert.assertTrue(logs.contains("Operation output: {\"success\":\"Logs correctly obtained\"}"));
            Assert.assertTrue(logs.contains("---------------------------------------------------------------------------------------------------------------"));
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void serviceGetLogsTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("username", "username");
            mapRequest.put("password", "password");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            client.newCall(request).execute();

            // Request Body has same parameters for Sign Up and Log In
            request = new Request.Builder().url(URL_LOGIN).put(body).build();
            client.newCall(request).execute();

            // The method to get logs receives nothing, so there is no need for a request body
            // When HTTP request type is absent, it defaults to GET
            request = new Request.Builder().url(URL_LOGS).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);

            String logs = mapResponse.get("logs");
            Assert.assertTrue(logs.contains("Operation ID: 1"));
            Assert.assertTrue(logs.contains("Operation name: SIGNUP"));
            Assert.assertTrue(logs.contains("Operation time:"));
            Assert.assertTrue(logs.contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(logs.contains("Operation output: {\"success\":\"User created successfully\"}"));
            Assert.assertTrue(logs.contains("---------------------------------------------------------------------------------------------------------------"));
            Assert.assertTrue(logs.contains("Operation ID: 2"));
            Assert.assertTrue(logs.contains("Operation name: LOGIN"));
            Assert.assertTrue(logs.contains("Operation time:"));
            Assert.assertTrue(logs.contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(logs.contains("Operation output: {\"success\":\"Login successful\",\"sessionId\":"));
            Assert.assertTrue(logs.contains("Operation ID: 3"));
            Assert.assertTrue(logs.contains("Operation name: LOGS"));
            Assert.assertTrue(logs.contains("Operation time:"));
            Assert.assertTrue(logs.contains("Operation input: {}"));
            Assert.assertTrue(logs.contains("Operation output: {\"success\":\"Logs correctly obtained\"}"));
            Assert.assertFalse(logs.contains("Operation ID: 4"));
            Assert.assertEquals(3, operations.getLogsLength());
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
