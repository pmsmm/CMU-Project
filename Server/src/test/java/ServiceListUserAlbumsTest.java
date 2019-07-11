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

public class ServiceListUserAlbumsTest {

    private static final int OK = 200;

    private static final int PORT = JavalinApp.PORT;

    private Javalin app = null;
    private OkHttpClient client = null;

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    private static final String URL_BASE = "http://localhost:" + PORT;
    private static final String URL_USER_ALBUMS = URL_BASE + "/useralbums";

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
    public void listAlbumsZeroAlbumsTest() {
        try {
            operations.addUser(new User("username2", "password"));
            session = new Session("username2", Operations.SESSION_DURATION);
            operations.addSession(session);

            String URL = URL_USER_ALBUMS + "/" + session.getSessionId() + "/username2";
            Request request = new Request.Builder().url(URL).get().build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("User albums successfully obtained", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("0", mapResponse.get("size"));
            Assert.assertEquals("", mapResponse.get("albums"));
            Assert.assertNull(mapResponse.get("Users_10"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void listAlbumsOneAlbumTest() {
        try {
            String URL = URL_USER_ALBUMS + "/" + session.getSessionId() + "/username";
            Request request = new Request.Builder().url(URL).get().build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("User albums successfully obtained", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("1", mapResponse.get("size"));
            Assert.assertEquals("10", mapResponse.get("albums"));
            Assert.assertEquals("album", mapResponse.get("10"));
            Assert.assertEquals("username", mapResponse.get("Users_10"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void listAlbumsTwoAlbumsTest() {
        try {
            operations.addAlbum(new Album("album2", 11), "username");
            operations.setSliceURL(session.getSessionId(), "username", "http://www.url.com", 11);

            String URL = URL_USER_ALBUMS + "/" + session.getSessionId() + "/username";
            Request request = new Request.Builder().url(URL).get().build();
            Response response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("User albums successfully obtained", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("2", mapResponse.get("size"));
            Assert.assertEquals("10,11", mapResponse.get("albums"));
            Assert.assertEquals("album", mapResponse.get("10"));
            Assert.assertEquals("album2", mapResponse.get("11"));
            Assert.assertEquals("username", mapResponse.get("Users_10"));
            Assert.assertEquals("username", mapResponse.get("Users_11"));

            operations.addUser(new User("username2", "password"));
            Session session2 = new Session("username2", Operations.SESSION_DURATION);
            operations.addSession(session2);
            operations.addUserToAlbum(10, "username", "username2");

            URL = URL_USER_ALBUMS + "/" + session2.getSessionId() + "/username2";
            request = new Request.Builder().url(URL).get().build();
            response = client.newCall(request).execute();

            Assert.assertEquals(OK, response.code());
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("User albums successfully obtained", mapResponse.get("success"));
            Assert.assertNull(mapResponse.get("error"));
            Assert.assertEquals("1", mapResponse.get("size"));
            Assert.assertEquals("10", mapResponse.get("albums"));
            Assert.assertEquals("album", mapResponse.get("10"));
            Assert.assertEquals("username2,username", mapResponse.get("Users_10"));
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
