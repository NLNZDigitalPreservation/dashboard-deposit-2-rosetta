package nz.govt.natlib.dashboard.ui.command;

public class UserAccessReqCommand {
    private String username;
    private String password;
    private String email;
    private String presentationName;
    private String token;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPresentationName() {
        return presentationName;
    }

    public void setPresentationName(String presentationName) {
        this.presentationName = presentationName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
