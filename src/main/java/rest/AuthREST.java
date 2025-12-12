package rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jwtrest.TokenProvider;
import ejb.AdminEJBLocal;

@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthREST {

    @Inject
    private AdminEJBLocal adminEJB;

    @Inject
    private TokenProvider tokenProvider;

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserLogin login) {

        // Use username instead of email
        boolean isValid = adminEJB.login(login.getUsername(), login.getPassword());

        if (!isValid) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"Invalid username or password\"}")
                    .build();
        }

        String token = tokenProvider.generateToken("Admin");

        return Response.ok("{\"token\":\"" + token + "\"}").build();
    }

    public static class UserLogin {
        private String username; // Changed from email
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
