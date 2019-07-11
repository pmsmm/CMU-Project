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

public class ServiceSignUpTest {

    private static final int CREATED = 201;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_SIGNUP = URL_BASE + "/signup";

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
    public void validSignUpRequestTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("username", "username");
            mapRequest.put("password", "password");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("User created successfully", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameSignUpRequestTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("username", "");
            mapRequest.put("password", "password");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Username cannot be empty", mapResponse.get("error"));
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void repeatedUsernameSignUpRequestTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("username", "username");
            mapRequest.put("password", "password");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("User created successfully", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());

            request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Username already exists", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(2, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPasswordSignUpRequestTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("username", "username");
            mapRequest.put("password", "x");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters", mapResponse.get("error"));
            Assert.assertEquals(0, operations.getUsersLength());
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
