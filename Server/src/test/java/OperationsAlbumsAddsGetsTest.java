import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

public class OperationsAlbumsAddsGetsTest {

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
    public void albumAddNewTest() {
        try {
            Assert.assertEquals(0, operations.getAlbumsLength());
            operations.addUser(new User("username", "password"));
            operations.addUser(new User("username2", "password"));

            String[] returnValues = operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("10", returnValues[1]);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getUserByUsername("username").getUserAlbumNumber());
            Assert.assertEquals(10, (int) operations.getUserByUsername("username").getAlbums().get(0));
            Assert.assertEquals(1, operations.getAlbumById(10).getAlbumUserNumber());
            Assert.assertTrue(operations.getAlbumById(10).isUserInAlbum("username"));
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username"));

            returnValues = operations.addAlbum(new Album("album", 11), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("11", returnValues[1]);
            Assert.assertEquals(2, operations.getAlbumsLength());
            Assert.assertEquals(2, operations.getUserByUsername("username").getUserAlbumNumber());
            Assert.assertEquals(11, (int) operations.getUserByUsername("username").getAlbums().get(1));
            Assert.assertNull(operations.getAlbumById(11).getSliceURL("username"));

            returnValues = operations.addAlbum(new Album("album", 12), "username2");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("12", returnValues[1]);
            Assert.assertEquals(3, operations.getAlbumsLength());
            Assert.assertEquals(1, operations.getUserByUsername("username2").getUserAlbumNumber());
            Assert.assertEquals(11, (int) operations.getUserByUsername("username").getAlbums().get(1));
            Assert.assertEquals(1, operations.getAlbumById(12).getAlbumUserNumber());
            Assert.assertTrue(operations.getAlbumById(12).isUserInAlbum("username2"));
            Assert.assertNull(operations.getAlbumById(12).getSliceURL("username2"));

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"10\":{\"id\":10,\"slices\":{},\"name\":\"album\"},\"11\":{\"id\":11,\"slices\":{},\"name\":\"album\"},\"12\":{\"id\":12,\"slices\":{},\"name\":\"album\"}},\"users\":{\"username2\":{\"username\":\"username2\",\"password\":\"password\",\"albums\":[12],\"sessionId\":0},\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[10,11],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);

            operations = null;
            Operations.cleanServer();

            PrintWriter writer = new PrintWriter(Operations.STATE_BACKUP_PATH);
            writer.println(jsonString);
            writer.close();

            operations = Operations.getServer();
            Assert.assertEquals(3, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(2, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void albumAddNullAlbumTest() {
        try {
            operations.addUser(new User("username", "password"));
            String[] returnValues = operations.addAlbum(null, "username");
            Assert.assertEquals("Album cannot be null", returnValues[0]);
            Assert.assertNull(returnValues[1]);

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            returnValues = operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("10", returnValues[1]);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(10, (int) operations.getUserByUsername("username").getAlbums().get(0));
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void albumAddAlbumEmptyNameTest() {
        try {
            operations.addUser(new User("username", "password"));

            String[] returnValues = operations.addAlbum(new Album("", 10), "username");
            Assert.assertEquals("Album name cannot be empty or null", returnValues[0]);
            Assert.assertNull(returnValues[1]);

            returnValues = operations.addAlbum(new Album(null, 10), "username");
            Assert.assertEquals("Album name cannot be empty or null", returnValues[0]);
            Assert.assertNull(returnValues[1]);

            returnValues = operations.addAlbum(new Album("      ", 10), "username");
            Assert.assertEquals("Album name cannot be empty or null", returnValues[0]);
            Assert.assertNull(returnValues[1]);

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            returnValues = operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("10", returnValues[1]);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(10, (int) operations.getUserByUsername("username").getAlbums().get(0));
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void albumAddInvalidUsernameTest() {
        try {
            operations.addUser(new User("username", "password"));
            String[] returnValues = operations.addAlbum(new Album("album", 10), "username2");
            Assert.assertEquals("Username does not exist or is invalid", returnValues[0]);
            Assert.assertNull(returnValues[1]);

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);

            Assert.assertEquals(0, operations.getAlbumsLength());
            Assert.assertEquals(0, operations.getSessionsLength());
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(0, operations.getLogsLength());

            returnValues = operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals("Album successfully added", returnValues[0]);
            Assert.assertEquals("10", returnValues[1]);
            Assert.assertEquals(1, operations.getAlbumsLength());
            Assert.assertEquals(10, (int) operations.getUserByUsername("username").getAlbums().get(0));
            Assert.assertNull(operations.getAlbumById(10).getSliceURL("username"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addSliceURLtoExistingAlbum() {
        try {
            operations.addUser(new User("username", "password"));
            Album album = new Album("album", 10);
            operations.addAlbum(album, "username");

            Assert.assertEquals("URL successfully set", operations.addSliceURLtoAlbum(10, "URL", "username"));
            Assert.assertEquals("URL", album.getSliceURL("username"));

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"10\":{\"id\":10,\"slices\":{\"username\":\"URL\"},\"name\":\"album\"}},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[10],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addNullEmptySliceURLtoAlbum() {
        try {
            operations.addUser(new User("username", "password"));
            Album album = new Album("album", 10);
            operations.addAlbum(album, "username");

            Assert.assertEquals("URL must not be null or empty", operations.addSliceURLtoAlbum(10, null, "username"));
            Assert.assertEquals("URL must not be null or empty", operations.addSliceURLtoAlbum(10, "", "username"));
            Assert.assertEquals("URL must not be null or empty", operations.addSliceURLtoAlbum(10, "    ", "username"));
            Assert.assertNull(album.getSliceURL("username"));

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"10\":{\"id\":10,\"slices\":{},\"name\":\"album\"}},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[10],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addSliceURLtoAlbumUserDoesNotBelongTo() {
        try {
            operations.addUser(new User("username", "password"));
            Album album = new Album("album", 10);
            operations.addAlbum(album, "username");

            Assert.assertEquals("User does not belong to the album", operations.addSliceURLtoAlbum(10, "URL", "username2"));
            Assert.assertNull(album.getSliceURL("username"));
            Assert.assertEquals("User is not in album", album.getSliceURL("username2"));

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"10\":{\"id\":10,\"slices\":{},\"name\":\"album\"}},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[10],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addSliceURLtoInvalidAlbum() {
        try {
            operations.addUser(new User("username", "password"));
            Album album = new Album("album", 10);
            operations.addAlbum(album, "username");

            Assert.assertEquals("Album id is invalid or does not exist", operations.addSliceURLtoAlbum(-1, "URL", "username"));
            Assert.assertEquals("Album id is invalid or does not exist", operations.addSliceURLtoAlbum(0, "URL", "username"));
            Assert.assertEquals("Album id is invalid or does not exist", operations.addSliceURLtoAlbum(9, "URL", "username"));
            Assert.assertEquals("Album id is invalid or does not exist", operations.addSliceURLtoAlbum(11, "URL", "username"));
            Assert.assertNull(album.getSliceURL("username"));

            String jsonString = FileUtils.readFileToString(new File(Operations.STATE_BACKUP_PATH), "UTF-8");
            jsonString = jsonString.replace("\n", "").replace("\r", "");
            Assert.assertEquals("{\"albums\":{\"10\":{\"id\":10,\"slices\":{},\"name\":\"album\"}},\"users\":{\"username\":{\"username\":\"username\",\"password\":\"password\",\"albums\":[10],\"sessionId\":0}},\"sessions\":{},\"logs\":\"\",\"counterAlbum\":0,\"counterLog\":0}", jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getAlbumsTest() {
        try {
            Assert.assertEquals(0, operations.getAlbums().size());
            Assert.assertEquals(0, operations.getAlbumsLength());
            operations.addUser(new User("username", "password"));
            operations.addAlbum(new Album("album", 10), "username");
            Assert.assertEquals(1, operations.getAlbums().size());
            Assert.assertEquals(1, operations.getAlbumsLength());
            operations.addAlbum(new Album("album", 11), "username");
            Assert.assertEquals(2, operations.getAlbums().size());
            Assert.assertEquals(2, operations.getAlbumsLength());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getAlbumByIdTest() {
        try {
            Assert.assertNull(operations.getAlbumById(0));
            Assert.assertNull(operations.getAlbumById(1));
            operations.addUser(new User("username", "password"));
            operations.addAlbum(new Album("album", 1), "username");
            Assert.assertNull(operations.getAlbumById(0));
            Assert.assertEquals("album", operations.getAlbumById(1).getName());
            Assert.assertNull(operations.getAlbumById(2));
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
