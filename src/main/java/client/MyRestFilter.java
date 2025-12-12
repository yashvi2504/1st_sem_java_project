
package client;

import java.io.IOException;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import record.KeepRecord;

@Provider
@PreMatching
public class MyRestFilter implements ClientRequestFilter {
    @Inject KeepRecord keepRecord;

    public MyRestFilter() {}

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String token = keepRecord.getToken();
        if (token != null) {
            requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
    }
}
