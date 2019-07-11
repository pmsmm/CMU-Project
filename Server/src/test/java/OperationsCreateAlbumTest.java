import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class OperationsCreateAlbumTest {

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

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
            HashMap<String, String> response = operations.createAlbum(session.getSessionId(), user.getUsername(), "album");

            Assert.assertEquals("Album successfully added", response.get("success"));
            Assert.assertNull(response.get("error"));
            Assert.assertEquals("1", response.get("albumId"));

            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: CREATE_ALBUM"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"" + session.getSessionId() + "\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Album successfully added\",\"albumId\":\"1\""));

            //An user can create albums with the same name
            response = operations.createAlbum(session.getSessionId(), user.getUsername(), "album");

            Assert.assertEquals("Album successfully added", response.get("success"));
            Assert.assertNull(response.get("error"));
            Assert.assertEquals("2", response.get("albumId"));

            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(2, operations.getAlbumsLength());
            Assert.assertEquals(2, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"Album successfully added\",\"albumId\":\"2\""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidSessionIdCreateAlbumTest() {
        try {
            HashMap<String, String> response1 = operations.createAlbum(0, user.getUsername(), "album");
            HashMap<String, String> response2 = operations.createAlbum(-1, user.getUsername(), "album");
            HashMap<String, String> response3 = operations.createAlbum(session.getSessionId()-1, user.getUsername(), "album");
            HashMap<String, String> response4 = operations.createAlbum(session.getSessionId()+1, user.getUsername(), "album");

            Assert.assertNull(response1.get("success"));
            Assert.assertEquals("Invalid session id", response1.get("error"));
            Assert.assertNull(response2.get("success"));
            Assert.assertEquals("Invalid session id", response2.get("error"));
            Assert.assertNull(response3.get("success"));
            Assert.assertEquals("Invalid session id", response3.get("error"));
            Assert.assertNull(response4.get("success"));
            Assert.assertEquals("Invalid session id", response4.get("error"));

            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(4, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 4"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: CREATE_ALBUM"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"0\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"-1\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"" + (session.getSessionId()-1) + "\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"" + (session.getSessionId()+1) + "\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Invalid session id\""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameCreateAlbumTest() {
        try {
            HashMap<String, String> response1 = operations.createAlbum(session.getSessionId(), null, "album");
            HashMap<String, String> response2 = operations.createAlbum(session.getSessionId(), "", "album");
            HashMap<String, String> response3 = operations.createAlbum(session.getSessionId(), "       ", "album");
            HashMap<String, String> response4 = operations.createAlbum(session.getSessionId(), "username2", "album");
            operations.addUser(new User("username2", "password"));
            HashMap<String, String> response5 = operations.createAlbum(session.getSessionId(), "username2", "album");

            Assert.assertNull(response1.get("success"));
            Assert.assertEquals("Invalid username", response1.get("error"));
            Assert.assertNull(response2.get("success"));
            Assert.assertEquals("Invalid username", response2.get("error"));
            Assert.assertNull(response3.get("success"));
            Assert.assertEquals("Invalid username", response3.get("error"));
            Assert.assertNull(response4.get("success"));
            Assert.assertEquals("Invalid username", response4.get("error"));
            Assert.assertNull(response5.get("success"));
            Assert.assertEquals("Invalid session id", response5.get("error"));

            Assert.assertEquals(2, operations.getUsersLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(5, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 4"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 5"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: CREATE_ALBUM"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"" + session.getSessionId() + "\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"" + session.getSessionId() + "\",\"username\":\"\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"" + session.getSessionId() + "\",\"username\":\"       \"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"album\",\"sessionId\":\"" + session.getSessionId() + "\",\"username\":\"username2\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Invalid username\""));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Invalid session id\""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidAlbumNameCreateAlbumTest() {
        try {
            HashMap<String, String> response1 = operations.createAlbum(session.getSessionId(), "username", null);
            HashMap<String, String> response2 = operations.createAlbum(session.getSessionId(), "username", "");
            HashMap<String, String> response3 = operations.createAlbum(session.getSessionId(), "username", "     ");

            Assert.assertNull(response1.get("success"));
            Assert.assertEquals("Album name cannot be empty or null", response1.get("error"));
            Assert.assertNull(response2.get("success"));
            Assert.assertEquals("Album name cannot be empty or null", response2.get("error"));
            Assert.assertNull(response3.get("success"));
            Assert.assertEquals("Album name cannot be empty or null", response3.get("error"));

            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getSessionsLength());
            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(3, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: CREATE_ALBUM"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"sessionId\":\"" + session.getSessionId() + "\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"\",\"sessionId\":\"" + session.getSessionId() + "\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"albumName\":\"     \",\"sessionId\":\"" + session.getSessionId() + "\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Album name cannot be empty or null\""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        user = null;
        session = null;

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
