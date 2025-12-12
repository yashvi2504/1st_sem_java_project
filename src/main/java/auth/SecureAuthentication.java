
package auth;

import beans.LoginBean;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static jwtrest.Constants.AUTHORIZATION_HEADER;
import static jwtrest.Constants.BEARER;
import jwtrest.JWTCredential;
import jwtrest.TokenProvider;
import record.KeepRecord;
import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.security.Principal;
import java.util.Set;
import java.util.HashSet;

@Named
@RequestScoped
public class SecureAuthentication implements HttpAuthenticationMechanism, Serializable {

    @Inject
    TokenProvider tokenProvider;

    @Inject
    LoginBean lbean;

    @Inject
    KeepRecord keepRecord;

    // Use your persistence unit name (pharmacyPU or whatever your persistence.xml declares)
    @PersistenceContext(unitName = "pharmacyPU")
    private EntityManager em;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext ctx) throws AuthenticationException {
    // Skip JSF auth for REST endpoints
if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
    return ctx.doNothing(); // let JAX-RS handle it
}
       

// handle logout
        
        
        try {
            if (request.getRequestURI().contains("Logout")) {
                try { request.logout(); } catch (Exception ex) { /* ignore */ }
                keepRecord.reset();
//                response.sendRedirect("Login.jsf");
response.sendRedirect(request.getContextPath() + "/Login.xhtml");

                return ctx.doNothing();
            }
        } catch (Exception e) { e.printStackTrace(); }

        // try to extract bearer token first
        String token = extractToken(ctx);

        try {
            // If we have no token and the request is a username/password form login:
            if (token == null && request.getParameter("username") != null) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");

                try {
                    // Query DB directly (plain-text password compare)
                    // NOTE: use email if that's what you expect; your Users entity has 'email' field.
                    Users user = em.createQuery(
                            "SELECT u FROM Users u WHERE u.username  = :u AND u.password = :p", Users.class)
                            .setParameter("u", username)
                            .setParameter("p", password)
                            .getSingleResult();

                    // Build Principal and roles set
                    final Principal principal = () -> user.getUsername(); // or user.getUsername() if you changed entity
                    Set<String> roles = new HashSet<>();
                    if (user.getRoleId() != null) {
                        // Role name from Roles entity, e.g. "Admin"
                        roles.add(user.getRoleId().getRoleName());
                    }

                    // Create JWT and store it + add header
                    String jwt = tokenProvider.createToken(principal.getName(), roles, false);
                    keepRecord.setToken(jwt);
                    keepRecord.setPrincipal(principal);
                    keepRecord.setRoles(roles);
                    contextResponseAddHeader(ctx, AUTHORIZATION_HEADER, BEARER + jwt);

                    // Tell container about login so @RolesAllowed works
                    AuthenticationStatus status = ctx.notifyContainerAboutLogin(principal, roles);

                    // Forward user to appropriate page
                    if (roles.contains("Admin")) {
//                        request.getRequestDispatcher("Admin.jsf").forward(request, response);
request.getRequestDispatcher("/Admin.xhtml").forward(request, response);

                    }else if(roles.contains("Delivery")) {
//                        request.getRequestDispatcher("Delivery/delivery.jsf").forward(request, response);
                        request.getRequestDispatcher("/delivery.xhtml").forward(request, response);

                    }
                    else {
//                        request.getRequestDispatcher("index.xhtml").forward(request, response);
response.sendRedirect(request.getContextPath() + "/index.xhtml");

                    }

                    return status;

                } catch (NoResultException nre) {
                    // invalid credentials
                    keepRecord.setErrorStatus("Either Username or Password is wrong !");
//                    response.sendRedirect("Login.jsf");
response.sendRedirect(request.getContextPath() + "/Login.xhtml");

                    return ctx.doNothing();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    keepRecord.setErrorStatus("Login error: " + ex.getMessage());
//                    response.sendRedirect("Login.jsf");
response.sendRedirect(request.getContextPath() + "/Login.xhtml");

                    return ctx.doNothing();
                }
            }

            // If we already have a token in session, set container identity
            if (keepRecord.getToken() != null) {
                ctx.notifyContainerAboutLogin(keepRecord.getPrincipal(), keepRecord.getRoles());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // if there is a bearer token, validate it:
        if (token != null) {
            return validateToken(token, ctx);
        } else if (ctx.isProtected()) {
            // protected resource requested but no credentials, deny
            return ctx.responseUnauthorized();
        }

        return ctx.doNothing();
    }

    private void contextResponseAddHeader(HttpMessageContext ctx, String name, String value) {
        // helper — add header to response (works similarly to context.getResponse().addHeader)
        try {
            ctx.getResponse().addHeader(name, value);
        } catch (Exception e) {
            // fallback ignored
        }
    }

    private AuthenticationStatus validateToken(String token, HttpMessageContext context) {
        try {
            if (tokenProvider.validateToken(token)) {
                JWTCredential credential = tokenProvider.getCredential(token);
                return context.notifyContainerAboutLogin(credential.getPrincipal(), credential.getAuthorities());
            }
            return context.responseUnauthorized();
        } catch (ExpiredJwtException eje) {
            return context.responseUnauthorized();
        }
    }

    // not used in DB login path, keep for token creation use if needed
    private AuthenticationStatus createTokenFromResult(String username, Set<String> roles, HttpMessageContext context) {
        String jwt = tokenProvider.createToken(username, roles, false);
        keepRecord.setToken(jwt);
        context.getResponse().addHeader(AUTHORIZATION_HEADER, BEARER + jwt);
        return context.notifyContainerAboutLogin(() -> username, roles);
    }

    private String extractToken(HttpMessageContext context) {
        String authorizationHeader = context.getRequest().getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            String token = authorizationHeader.substring(BEARER.length());
            keepRecord.setToken(token);
            return token;
        }
        return null;
    }
}
