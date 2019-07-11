import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AlbumTest {

    private Album album = null;

    @Before
    public void setUp() {
        album = new Album("album", 1);
    }

    @Test
    public void albumConstructorTest() {
        try {
            Assert.assertEquals(0, album.getAlbumUserNumber());
            Assert.assertEquals("album", album.getName());
            Assert.assertEquals(1, album.getId());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void userInAlbumTest() {
        try {
            album.addUserToAlbum("user", "URL");
            Assert.assertTrue(album.isUserInAlbum("user"));
            Assert.assertEquals("URL", album.getSliceURL("user"));
            Assert.assertEquals(1, album.getAlbumUserNumber());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void userNotInAlbum() {
        try {
            Assert.assertFalse(album.isUserInAlbum("anotherUser"));
            Assert.assertEquals("User is not in album", album.getSliceURL("anotherUser"));
            Assert.assertEquals(0, album.getAlbumUserNumber());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getSliceURLfromValidUser() {
        try {
            album.addUserToAlbum("username", "URL");
            Assert.assertEquals("URL", album.getSliceURL("username"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getSliceURLfromInvalidUser() {
        try {
            album.addUserToAlbum("username", "URL");
            Assert.assertEquals("User is not in album", album.getSliceURL("username2"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void getSliceURLfromNullUser() {
        try {
            album.addUserToAlbum("username", "URL");
            Assert.assertEquals("User is not in album", album.getSliceURL(null));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addNotEmptyUserToAlbum() {
        try {
            album.addUserToAlbum("username", null);
            album.addUserToAlbum("username2", "URL");
            Assert.assertEquals(2, album.getAlbumUserNumber());
            Assert.assertNull(album.getSliceURL("username"));
            Assert.assertEquals("URL", album.getSliceURL("username2"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void addNullEmptyUserToAlbum() {
        try {
            album.addUserToAlbum(null, "URL");
            album.addUserToAlbum("", "URL");
            album.addUserToAlbum("      ", "URL");
            Assert.assertEquals(0, album.getAlbumUserNumber());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        album = null;
    }
}
