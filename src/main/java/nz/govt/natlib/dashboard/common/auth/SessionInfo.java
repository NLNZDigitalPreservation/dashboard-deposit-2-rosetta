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

    public SessionInfo() {
    }

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

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpireInterval() {
        return expireInterval;
    }

    public void setExpireInterval(long expireInterval) {
        this.expireInterval = expireInterval;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}