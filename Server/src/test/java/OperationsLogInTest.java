import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class OperationsLogInTest {

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
        operations = Operations.getServer();
    }

    @Test
    public void validLogInWithPreviousSessionTest() {
        try {
            User user = new User("username", "password");
            operations.addUser(new User("username", "password"));
            Session session = new Session("username", Operations.SESSION_DURATION);
            operations.addSession(session);
            user.setSessionId(session.getSessionId());

            HashMap<String, String> response = operations.logIn("username", "password");
            Assert.assertEquals("Login successful", response.get("success"));
            Assert.assertNull(response.get("error"));
            Assert.assertEquals(String.valueOf(session.getSessionId()), response.get("sessionId"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGIN"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Login successful\",\"sessionId\":"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void validLogInWithoutPreviousSessionTest() {
        try {
            User user = new User("username", "password");
            operations.addUser(user);

            HashMap<String, String> response = operations.logIn("username", "password");
            Assert.assertEquals("Login successful", response.get("success"));
            Assert.assertNull(response.get("error"));
            Assert.assertTrue(Integer.valueOf(response.get("sessionId")) > 0);

            Assert.assertTrue(operations.getUserByUsername("username").getSessionId() > 0);
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGIN"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Login successful\",\"sessionId\":"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nullUsernameLogInTest() {
        try {
            Assert.assertEquals("The Inserted Username is Incorrect!", operations.logIn(null, "password").get("error"));
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGIN"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"The Inserted Username is Incorrect!\"}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nonExistingUsernameLogInTest() {
        try {
            Assert.assertEquals("The Inserted Username is Incorrect!", operations.logIn("username", "password").get("error"));
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGIN"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"The Inserted Username is Incorrect!\"}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nullPasswordLogInTest() {
        try {
            User user = new User("username", "password");
            operations.addUser(user);
            Assert.assertEquals("Invalid Password! Please Try Again", operations.logIn("username", null).get("error"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGIN"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Invalid Password! Please Try Again\"}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void incorrectPasswordLogInTest() {
        try {
            User user = new User("username", "password");
            operations.addUser(user);
            Assert.assertEquals("Invalid Password! Please Try Again", operations.logIn("username", "incorrectPassword").get("error"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: LOGIN"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"incorrectPassword\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Invalid Password! Please Try Again\"}"));
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
