package jwtrest;

import java.io.Serializable;
import java.util.Set;
import jakarta.security.enterprise.credential.Credential;

/**
 * Simple credential wrapper we use when creating token-based principal/roles
 */
public class JWTCredential implements Credential, Serializable {

    private final String principal;
    private final Set<String> authorities;

    public JWTCredential(String principal, Set<String> authorities) {
        this.principal = principal;
        this.authorities = authorities;
    }

    public String getPrincipal() {
        return principal;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }
}
