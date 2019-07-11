import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class SessionTest {

    private Session session = null;

    @Before
    public void setUp() {
        session = new Session("username", Operations.SESSION_DURATION);
    }

    @Test
    public void sessionConstructorTest() {
        try {
            Assert.assertEquals("username", session.getUsername());
            Assert.assertTrue(session.getSessionId() > 0 && session.getSessionId() < Session.MAX_SESSION_ID);
            Assert.assertTrue((int) new Date().getTime() - (int) session.getLoginTime().getTime() < 1000);
            Assert.assertEquals(Operations.SESSION_DURATION, session.getSessionDuration());
            Assert.assertTrue(session.isSessionValid());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void sessionValidTest() {
        try {
            Session session = new Session("username", 10000);
            Thread.sleep(500);
            Assert.assertTrue(session.isSessionValid());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void sessionExpiredTest() {
        try {
            Session session = new Session("username", 100);
            Thread.sleep(500);
            Assert.assertFalse(session.isSessionValid());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        session = null;
    }
}
