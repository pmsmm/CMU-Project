import java.security.SecureRandom;
import java.util.Date;

public class Session {

    protected static final int MAX_SESSION_ID = (int) Math.pow(2, 32);

    private String username;
    private int sessionId;
    private Date loginTime;
    private int sessionDuration;

    public Session(String username, int sessionDuration) {
        this.username = username;
        while (this.sessionId == 0)
            this.sessionId = new SecureRandom().nextInt(MAX_SESSION_ID);
        this.loginTime = new Date();
        this.sessionDuration = sessionDuration;
    }

    public String getUsername() {
        return username;
    }

    public int getSessionId() {
        return sessionId;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public int getSessionDuration() {
        return sessionDuration;
    }

    public boolean isSessionValid() {
        return (new Date().getTime() < loginTime.getTime() + sessionDuration);
    }
}
