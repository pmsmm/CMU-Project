import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class OperationsSingletonTest {

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

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
    }

    @Test
    public void singletonGetEmptyServerTest() {
        try {
            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
            operations = null;
            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
            operations = null;
            Operations.cleanServer();
            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void singletonGetNonEmptyServerTest() {
        try {
            Session session = new Session("user1", Operations.SESSION_DURATION);
            HashMap<String, String> appRequest = new HashMap<>();
            appRequest.put("username", "username");
            appRequest.put("password", "password");
            HashMap<String, String> appResponse = new HashMap<>();
            appResponse.put("success", "Success");

            operations = Operations.getServer();
            operations.addUser(new User("user1", "password1"));
            operations.addUser(new User("user2", "password2"));
            operations.addUser(new User("user3", "password3"));
            operations.addSession(session);
            operations.addSession(new Session("user2", Operations.SESSION_DURATION + 5));
            operations.addAlbum(new Album("album", 1), "user1");
            operations.addLog("operation", appRequest, appResponse);

            operations = null;
            operations = Operations.getServer();
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(2, operations.getSessionsLength());
            Assert.assertEquals(3, operations.getUsersLength());
            Assert.assertEquals("album", operations.getAlbumById(1).getName());
            Assert.assertEquals(Operations.SESSION_DURATION, operations.getSessionById(session.getSessionId()).getSessionDuration());
            Assert.assertEquals("password3", operations.getUserByUsername("user3").getPassword());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: operation"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Success\"}"));

            operations = null;
            Operations.cleanServer();
            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
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
