import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class OperationsSignUpTest {

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
    public void validSignUp() {
        try {
            HashMap<String, String> response = operations.signUp("username", "password");
            Assert.assertEquals("User created successfully", response.get("success"));
            Assert.assertNull(response.get("error"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertEquals("username", operations.getUserByUsername("username").getUsername());
            Assert.assertEquals("password", operations.getUserByUsername("username").getPassword());
            Assert.assertEquals(0, operations.getUserByUsername("username").getUserAlbumNumber());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"User created successfully\"}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidUsernameTest() {
        try {
            Assert.assertEquals("Username cannot be null", operations.signUp(null, "password").get("error"));
            Assert.assertEquals("Username cannot be empty", operations.signUp("", "password").get("error"));
            Assert.assertEquals("Username cannot be empty", operations.signUp("    ", "password").get("error"));
            Assert.assertEquals("Username must only contain digits and letters", operations.signUp("username%", "password").get("error"));
            Assert.assertEquals("Username must have at least " + Operations.MIN_USERNAME_LENGTH + " characters", operations.signUp("x", "password").get("error"));
            Assert.assertEquals("Username must have at most " + Operations.MAX_USERNAME_LENGTH + " characters", operations.signUp("123456789012345678901234567890123456789012345678901234567890", "password").get("error"));
            Assert.assertEquals(0, operations.getUsersLength());
            operations.signUp("username", "password");
            Assert.assertEquals("Username already exists", operations.signUp("username", "password").get("error"));
            Assert.assertEquals(1, operations.getUsersLength());
            Assert.assertEquals(8, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 4"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 5"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 6"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 7"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 8"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 9"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"    \"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"username%\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"x\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"123456789012345678901234567890123456789012345678901234567890\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"password\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username cannot be null\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username cannot be empty\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username must only contain digits and letters\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username must have at least " + Operations.MIN_USERNAME_LENGTH + " characters\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username must have at most " + Operations.MAX_USERNAME_LENGTH + " characters\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"success\":\"User created successfully\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username already exists\"}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void invalidPasswordTest() {
        try {
            Assert.assertEquals("Password cannot be null", operations.signUp("username", null).get("error"));
            Assert.assertEquals("Password cannot be empty", operations.signUp("username", "").get("error"));
            Assert.assertEquals("Password cannot be empty", operations.signUp("username", "    ").get("error"));
            Assert.assertEquals("Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters", operations.signUp("username", "x").get("error"));
            Assert.assertEquals("Password must have at most " + Operations.MAX_PASSWORD_LENGTH + " characters", operations.signUp("username", "123456789012345678901234567890123456789012345678901234567890").get("error"));
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(5, operations.getLogsLength());

            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 3"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 4"));
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 5"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 6"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"    \",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"x\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"123456789012345678901234567890123456789012345678901234567890\",\"username\":\"username\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password cannot be null\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password cannot be empty\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password must have at least " + Operations.MIN_PASSWORD_LENGTH + " characters\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Password must have at most " + Operations.MAX_PASSWORD_LENGTH + " characters\"}"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void allInvalidParameters() {
        try {
            Assert.assertEquals("Username cannot be null", operations.signUp(null, "x").get("error"));
            Assert.assertEquals(0, operations.getUsersLength());
            Assert.assertEquals(1, operations.getLogsLength());
            Assert.assertTrue(operations.getLogs().contains("Operation ID: 1"));
            Assert.assertFalse(operations.getLogs().contains("Operation ID: 2"));
            Assert.assertTrue(operations.getLogs().contains("Operation name: SIGNUP"));
            Assert.assertTrue(operations.getLogs().contains("Operation time:"));
            Assert.assertTrue(operations.getLogs().contains("Operation input: {\"password\":\"x\"}"));
            Assert.assertTrue(operations.getLogs().contains("Operation output: {\"error\":\"Username cannot be null\"}"));
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
