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

public class ServiceCreateAlbumTest {

    private static final int CREATED = 201;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_CREATE_ALBUM = URL_BASE + "/createalbum";

    User user;
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

        user = new User("username", "password");
        operations.addUser(new User("username", "password"));
        session = new Session("username", Operations.SESSION_DURATION);
        operations.addSession(session);
        user.setSessionId(session.getSessionId());
    }

    @Test
    public void successfulCreateAlbumTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("sessionId", String.valueOf(session.getSessionId()));
            mapRequest.put("username", user.getUsername());
            mapRequest.put("albumName", "album");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Album successfully added", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("1", mapResponse.get("albumId"));

            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals("album", operations.getAlbumById(1).getName());

            //Ensures user can create albums with same name

            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();

            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Album successfully added", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("2", mapResponse.get("albumId"));

            Assert.assertEquals(2, operations.getLogsLength());
            Assert.assertEquals(2, operations.getAlbumsLength());
            Assert.assertEquals("album", operations.getAlbumById(2).getName());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidSessionIdCreateAlbumTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("sessionId", String.valueOf(0));
            mapRequest.put("username", user.getUsername());
            mapRequest.put("albumName", "album");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid session id", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("sessionId", String.valueOf(-1));

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid session id", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(2, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("sessionId", String.valueOf(session.getSessionId()-1));

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid session id", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(3, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("sessionId", String.valueOf(session.getSessionId()+1));

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid session id", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(4, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameCreateAlbumTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("sessionId", String.valueOf(session.getSessionId()));
            mapRequest.put("username", null);
            mapRequest.put("albumName", "album");

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid username", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("username", "");

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid username", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(2, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("username", "    ");

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid username", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(3, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("username", "username2");

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid username", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(4, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            operations.addUser(new User("username2", "password"));
            mapRequest.put("username", "username2");

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Invalid session id", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(5, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidAlbumNameCreateAlbumTest() {
        try {
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("sessionId", String.valueOf(session.getSessionId()));
            mapRequest.put("username", "username");
            mapRequest.put("albumName", null);

            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Album name cannot be empty or null", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("albumName", "");

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Album name cannot be empty or null", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(2, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());

            mapRequest.put("albumName", "    ");

            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            Assert.assertEquals(CREATED, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Album name cannot be empty or null", mapResponse.get("error"));
            Assert.assertNull(mapResponse.get("success"));
            Assert.assertEquals(3, operations.getLogsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        user = null;
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
