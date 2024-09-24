package nz.govt.natlib.dashboard.common.auth;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basically a look-up table, containing a map of session IDs to session info structs
 */
@Component
public class Sessions {

    private final ConcurrentHashMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    /**
     * Add session and remove any expired sessions while we're at it
     */
    public void addSession(String id, String username, String role, long expireInterval) {
        if (sessionMap.containsKey(id)) {
            // Can't happen
            throw new RuntimeException(String.format("Session id %s already exists", id));
        }
        for (String key : sessionMap.keySet()) {
            if (sessionMap.get(key).expired()) {
                removeSession(id);
            }
        }
        sessionMap.put(id, new SessionInfo(username, role, expireInterval));
    }

    public void removeSession(String id) {
        sessionMap.remove(id);
    }

    /**
     * Get the username for this session if it's still valid and, if so, update the timestamp of last access
     */
    public String getUsername(String id) throws InvalidSessionException {
        if (!sessionMap.containsKey(id) || sessionMap.get(id).expired()) {
            sessionMap.remove(id);
            throw new InvalidSessionException(id);
        }
        sessionMap.get(id).touch();
        return sessionMap.get(id).username;
    }

    /**
     * Get the role for this session if it's still valid and, if so, update the timestamp of last access
     */
    public String getRole(String id) throws InvalidSessionException {
        if (!sessionMap.containsKey(id) || sessionMap.get(id).expired()) {
            sessionMap.remove(id);
            throw new InvalidSessionException(id);
        }
        sessionMap.get(id).touch();
        return sessionMap.get(id).role;
    }

    public static class InvalidSessionException extends Exception {
        public InvalidSessionException(String id) {
            super(String.format("Session with %s has expired", id));
        }
    }


    // Struct used as the value in the kv pair representing a session
    static class SessionInfo {
        Date modified;
        String username;
        String role;
        long expireInterval;

        public SessionInfo(String username, String role, long expireInterval) {
            this.modified = new Date();
            this.username = username;
            this.role = role;
            this.expireInterval = expireInterval;
        }

        boolean expired() {
            return System.currentTimeMillis() - modified.getTime() > expireInterval;
        }

        void touch() {
            modified = new Date();
        }
    }
}