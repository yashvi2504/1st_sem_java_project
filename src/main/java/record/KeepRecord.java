
package record;

import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.security.enterprise.credential.Credential;

@Named
@SessionScoped
public class KeepRecord implements Serializable {

    private String token;
    private Principal principal;
    private Set<String> roles;
    private Credential credential;
    private String errorStatus = "";

    public void reset() {
        token = null;
        principal = null;
        roles = null;
        credential = null;
        errorStatus = "";
    }

    // getters / setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Principal getPrincipal() { return principal; }
    public void setPrincipal(Principal principal) { this.principal = principal; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public Credential getCredential() { return credential; }
    public void setCredential(Credential credential) { this.credential = credential; }
    public String getErrorStatus() { return errorStatus; }
    public void setErrorStatus(String errorStatus) { this.errorStatus = errorStatus; }
}
