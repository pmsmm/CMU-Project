import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class OperationsGetStateTest {

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
    public void validNotEmptyStateBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("{\"albums\":{\"1\":{\"id\":1,\"slices\":{\"user1\":\"url\"},\"name\":\"album\"}},\"users\":{\"user1\":{\"username\":\"user1\",\"password\":\"password\",\"albums\":[1],\"sessionId\":1749358129},\"user2\":{\"username\":\"user2\",\"password\":\"password\",\"albums\":[],\"sessionId\":876096095},\"user3\":{\"username\":\"user3\",\"password\":\"password\",\"albums\":[],\"sessionId\":0}},\"sessions\":{\"1749358129\":{\"username\":\"user1\",\"sessionId\":1749358129,\"loginTime\":\"Apr 5, 2019 1:44:25 AM\",\"sessionDuration\":" + Operations.SESSION_DURATION + "},\"876096095\":{\"username\":\"user2\",\"sessionId\":876096095,\"loginTime\":\"Apr 5, 2019 1:44:25 AM\",\"sessionDuration\":" + Operations.SESSION_DURATION + "}},\"logs\":\"Operation ID: 1\\nOperation name: CREATE_ALBUM\\nOperation time: Fri Apr 05 01:44:25 BST 2019\\nOperation input: {\\\"sessionId\\\":0}\\nOperation output: {\\\"success\\\":\\\"Album successfully added\\\",\\\"sessionId\\\":0,\\\"albumId\\\":1}\\n---------------------------------------------------------------------------------------------------------------\\n\",\"counterAlbum\":1,\"counterLog\":1}");
            writer.close();
            Assert.assertEquals("{\"albums\":{\"1\":{\"id\":1,\"slices\":{\"user1\":\"url\"},\"name\":\"album\"}},\"users\":{\"user1\":{\"username\":\"user1\",\"password\":\"password\",\"albums\":[1],\"sessionId\":1749358129},\"user2\":{\"username\":\"user2\",\"password\":\"password\",\"albums\":[],\"sessionId\":876096095},\"user3\":{\"username\":\"user3\",\"password\":\"password\",\"albums\":[],\"sessionId\":0}},\"sessions\":{\"1749358129\":{\"username\":\"user1\",\"sessionId\":1749358129,\"loginTime\":\"Apr 5, 2019 1:44:25 AM\",\"sessionDuration\":" + Operations.SESSION_DURATION + "},\"876096095\":{\"username\":\"user2\",\"sessionId\":876096095,\"loginTime\":\"Apr 5, 2019 1:44:25 AM\",\"sessionDuration\":" + Operations.SESSION_DURATION + "}},\"logs\":\"Operation ID: 1\\nOperation name: CREATE_ALBUM\\nOperation time: Fri Apr 05 01:44:25 BST 2019\\nOperation input: {\\\"sessionId\\\":0}\\nOperation output: {\\\"success\\\":\\\"Album successfully added\\\",\\\"sessionId\\\":0,\\\"albumId\\\":1}\\n---------------------------------------------------------------------------------------------------------------\\n\",\"counterAlbum\":1,\"counterLog\":1}", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));
            operations = Operations.getServer();
            Assert.assertEquals(1, operations.getAlbums().size());
            Assert.assertEquals(2, operations.getSessions().size());
            Assert.assertEquals(3, operations.getUsers().size());
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{\"1\":{\"id\":1,\"slices\":{\"user1\":\"url\"},\"name\":\"album\"}},\"users\":{\"user1\":{\"username\":\"user1\",\"password\":\"password\",\"albums\":[1],\"sessionId\":1749358129},\"user2\":{\"username\":\"user2\",\"password\":\"password\",\"albums\":[],\"sessionId\":876096095},\"user3\":{\"username\":\"user3\",\"password\":\"password\",\"albums\":[],\"sessionId\":0}},\"sessions\":{\"1749358129\":{\"username\":\"user1\",\"sessionId\":1749358129,\"loginTime\":\"Apr 5, 2019 1:44:25 AM\",\"sessionDuration\":" + Operations.SESSION_DURATION + "},\"876096095\":{\"username\":\"user2\",\"sessionId\":876096095,\"loginTime\":\"Apr 5, 2019 1:44:25 AM\",\"sessionDuration\":" + Operations.SESSION_DURATION + "}},\"logs\":\"Operation ID: 1\\nOperation name: CREATE_ALBUM\\nOperation time: Fri Apr 05 01:44:25 BST 2019\\nOperation input: {\\\"sessionId\\\":0}\\nOperation output: {\\\"success\\\":\\\"Album successfully added\\\",\\\"sessionId\\\":0,\\\"albumId\\\":1}\\n---------------------------------------------------------------------------------------------------------------\\n\",\"counterAlbum\":1,\"counterLog\":1}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
            Assert.assertEquals("album", operations.getAlbumById(1).getName());
            Assert.assertEquals(Operations.SESSION_DURATION, operations.getSessionById(1749358129).getSessionDuration());
            Assert.assertEquals("password", operations.getUserByUsername("user3").getPassword());
            Assert.assertTrue(operations.getLogs().contains("CREATE_ALBUM"));
            Assert.assertEquals(1, operations.counterAlbum.get());
            Assert.assertEquals(1, operations.counterLog.get());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void validEmptyStateBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}");
            writer.close();
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void emptyBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("");
            writer.close();
            Assert.assertEquals("", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidBackupFileTest() {
        try {
            new File(Operations.STATE_BACKUP_PATH);
            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println("Invalid backup file");
            writer.close();
            Assert.assertEquals("Invalid backup file", FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                    replace("\n", "").replace("\r", ""));

            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void noBackupFileTest() {
        try {
            Assert.assertFalse(original.exists());
            operations = Operations.getServer();
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getSessions().size());
            Assert.assertEquals(0, operations.getUsers().size());
            Assert.assertEquals(0, operations.getLogsLength());
            Assert.assertEquals("{\"albums\":{},\"users\":{},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}",
                    FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8").
                            replace("\n", "").replace("\r", ""));
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
