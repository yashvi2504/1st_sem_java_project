
package config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/*
  Uncomment / adapt to your environment. This uses container-provided database identity store.
  Adjust dataSourceLookup, callerQuery and groupsQuery to your actual schema.
*/
@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "jdbc/pharmacy",
        callerQuery = "select password from users where username = ?",
        groupsQuery = "select GROUPNAME from groups where username = ?",
        hashAlgorithm = Pbkdf2PasswordHash.class,
        priority = 30)
@ApplicationScoped
public class Project { }
