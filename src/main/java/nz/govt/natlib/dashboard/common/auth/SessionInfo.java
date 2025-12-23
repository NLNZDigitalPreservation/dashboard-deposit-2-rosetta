package nz.govt.natlib.dashboard.common.auth;

import java.util.Date;

// Struct used as the value in the kv pair representing a session
public class SessionInfo {
    Date modified;
    String username;
    String role;
    long expireInterval;
    String sessionId;
    String displayName;

    public SessionInfo(String username, String role, long expireInterval, String sessionId, String displayName) {
        this.modified = new Date();
        this.username = username;
        this.role = role;
        this.expireInterval = expireInterval;
        this.sessionId = sessionId;
        this.displayName = displayName;
    }

    boolean expired() {
        return System.currentTimeMillis() - modified.getTime() > expireInterval;
    }

    void touch() {
        modified = new Date();
    }

    public String getDisplayName() {
        return displayName;
    }
}