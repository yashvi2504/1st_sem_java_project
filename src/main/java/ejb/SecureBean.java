package ejb;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;

@DeclareRoles({"Admin", "Supervisor"})
@Stateless
public class SecureBean {
    @RolesAllowed("Admin")
    public String saySecureHello() {
        return "Secure Hello from Secure Bean";
    }
}
