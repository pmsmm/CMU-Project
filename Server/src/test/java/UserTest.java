import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserTest {

    User user;

    @Before
    public void setUp() {
        user = new User("username", "password");
    }

    @Test
    public void userConstructorTest() {
        Assert.assertEquals("username", user.getUsername());
        Assert.assertEquals("password", user.getPassword());
        Assert.assertEquals(0, user.getUserAlbumNumber());
    }

    @Test
    public void userInAlbumTest() {
        try {
            user.addAlbumUserIsIn(1);
            Assert.assertEquals(1, user.getUserAlbumNumber());
            Assert.assertTrue(user.isUserInAlbum(1));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void userNotInAlbumTest() {
        try {
            Assert.assertEquals(0, user.getUserAlbumNumber());
            Assert.assertFalse(user.isUserInAlbum(1));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void correctPasswordVerifierTest() {
        try {
            Assert.assertTrue(user.isPasswordCorrect("password"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void nullPasswordVerifierTest() {
        try {
            Assert.assertFalse(user.isPasswordCorrect(null));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void incorrectPasswordVerifierTest() {
        try {
            Assert.assertFalse(user.isPasswordCorrect("incorrectPassword"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        user = null;
    }
}
