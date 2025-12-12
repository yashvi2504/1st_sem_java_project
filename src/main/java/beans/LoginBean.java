package beans;

import ejb.AdminEJBLocal;
import entity.Users;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import record.KeepRecord;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
// existing fields, e.g. username/password, user session, etc.

    private static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID";
    // Put secrets in config / env — do not hardcode in production
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:8080/ox_pharmacy/googleCallback";
    private static final String GOOGLE_SCOPE = "openid email profile";

    @Inject
    private KeepRecord keepRecord;

    @Inject
    private AdminEJBLocal adminEJB;

    private String username;
    private String password;
    private Users loggedUser;
    private String errorstatus;

    public LoginBean() {}

    // ------------------------------------------------
    // LOGIN
    // ------------------------------------------------
    public String login() {

        loggedUser = adminEJB.getUserByUsername(username);

        if (loggedUser != null && loggedUser.getPassword().equals(password)) {

            errorstatus = null;
            keepRecord.setErrorStatus(null);

            String role = loggedUser.getRoleId().getRoleName();

            // -----------------------
            // ROLE-BASED REDIRECT
            // -----------------------

            if (role.equalsIgnoreCase("Admin")) {
                return "Admin?faces-redirect=true";
            }
            
            if (role.equalsIgnoreCase("Delivery")) {
                return "delivery?faces-redirect=true";
            }

            // Default: CUSTOMER
            return "index?faces-redirect=true";
        }

        // Login failed
        errorstatus = "Invalid username or password!";
        keepRecord.setErrorStatus(errorstatus);

        return null;
    }

    // ------------------------------------------------
    // ADMIN PAGE ACCESS CHECK
    // ------------------------------------------------
    public void verifyAdminAccess(ComponentSystemEvent event) throws IOException {

        if (loggedUser == null) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("Login.xhtml");
            return;
        }

        String role = loggedUser.getRoleId().getRoleName();

        if (!"Admin".equalsIgnoreCase(role)) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("AccessDenied.xhtml");
        }
    }

    // ------------------------------------------------
    // LOGOUT
    // ------------------------------------------------
    public String logout() {

        loggedUser = null;
        username = null;
        password = null;

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        return "index?faces-redirect=true";
    }

    // ------------------------------------------------
    // Helper: Only Admin can see admin tools
    // ------------------------------------------------
    public boolean isAdmin() {
        Set<String> roles = keepRecord.getRoles();
        return roles != null && roles.contains("Admin");
    }

    // ------------------------------------------------
    // GETTERS & SETTERS
    // ------------------------------------------------
    public Users getLoggedUser() { return loggedUser; }

    public String getErrorStatus() { return errorstatus; }

    public void setErrorStatus(String status) {
        this.errorstatus = status;
        keepRecord.setErrorStatus(status);
    }


public void setLoggedUser(Users loggedUser) {
    this.loggedUser = loggedUser;
}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    
     public String getGoogleRedirectUrl() {
        String base = "https://accounts.google.com/o/oauth2/v2/auth";
        String redirect = base
            + "?client_id=" + URLEncoder.encode(GOOGLE_CLIENT_ID, StandardCharsets.UTF_8)
            + "&redirect_uri=" + URLEncoder.encode(GOOGLE_REDIRECT_URI, StandardCharsets.UTF_8)
            + "&response_type=code"
            + "&scope=" + URLEncoder.encode(GOOGLE_SCOPE, StandardCharsets.UTF_8)
            + "&prompt=select_account";
        return redirect;
    }

    // add a method to set logged in user in session
    public void setUserSession(Users u) {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", u);
    }

   
}
