import com.google.gson.Gson;
import com.squareup.okhttp.*;
import io.javalin.Javalin;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ServiceBaseTest {

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;

    private static final int PORT = JavalinApp.PORT;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Javalin app = null;
    private OkHttpClient client = null;

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
    }

    @Test
    public void rootPathTest() {
        try {
            Request request = new Request.Builder().url(URL_BASE).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(OK, response.code());
            Assert.assertEquals("{\"status\":\"OK\"}", response.body().string());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPathTest() {
        try {
            String url = URL_BASE +"/pagenotexisting";
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(NOT_FOUND, response.code());
            Assert.assertEquals("This link does not exist", response.body().string());
        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nullBodyPostRequestTest() {
        try {
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(null));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(BAD_REQUEST, response.code());
            Assert.assertEquals(0, Operations.getServer().getUsersLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidBodyPostRequestTest() {
        try {
            RequestBody body = RequestBody.create(JSON, new Gson().toJson("invalidBody"));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            Response response = client.newCall(request).execute();
            Assert.assertEquals(BAD_REQUEST, response.code());
            Assert.assertEquals(0, Operations.getServer().getUsersLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        this.app.stop();
        Operations.cleanServer(); //Also deletes server backup file
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
