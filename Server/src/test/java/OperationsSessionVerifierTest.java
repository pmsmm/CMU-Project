import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class OperationsSessionVerifierTest {

    private Operations operations = null;
    private File original = null;
    private File temporary = null;

    Session session = null;
    Session session2 = null;

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

        session = new Session("username", Operations.SESSION_DURATION);
        session2 = new Session("username2", Operations.SESSION_DURATION);
        operations.addUser(new User("username", "password"));
        operations.addUser(new User("username2", "password2"));
        operations.addSession(session);
        operations.addSession(session2);
    }

    @Test
    public void validSessionTest() {
        try {
            Assert.assertEquals("Valid session", operations.sessionVerifier("username", session.getSessionId()));
            Assert.assertEquals("Valid session", operations.sessionVerifier("username2", session2.getSessionId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameTest() {
        try {
            Assert.assertEquals("Invalid username", operations.sessionVerifier(null, session.getSessionId()));
            Assert.assertEquals("Invalid username", operations.sessionVerifier("", session.getSessionId()));
            Assert.assertEquals("Invalid username", operations.sessionVerifier("    ", session.getSessionId()));
            Assert.assertEquals("Invalid username", operations.sessionVerifier("username3", session.getSessionId()));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void zeroNegativeSessionIdTest() {
        try {
            Assert.assertEquals("Invalid session id", operations.sessionVerifier("username", 0));
            Assert.assertEquals("Invalid session id", operations.sessionVerifier("username", -1));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidSessionIdTest() {
        try {
            Assert.assertEquals("Invalid session id", operations.sessionVerifier("username", session.getSessionId() - 1));
            Assert.assertEquals("Invalid session id", operations.sessionVerifier("username", session.getSessionId() + 1));
            Assert.assertEquals("Invalid session id", operations.sessionVerifier("username", session2.getSessionId() - 1));
            Assert.assertEquals("Invalid session id", operations.sessionVerifier("username", session2.getSessionId() + 1));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        session = null;
        session2 = null;

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
