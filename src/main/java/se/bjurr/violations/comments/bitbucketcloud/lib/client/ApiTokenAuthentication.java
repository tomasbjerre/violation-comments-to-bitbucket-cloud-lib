package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;

public class ApiTokenAuthentication implements ClientRequestFilter {
  private final String authHeader;

  /**
   * @param apiToken API Token
   */
  public ApiTokenAuthentication(final String apiToken) {
    this.authHeader = apiToken;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + authHeader);
  }
}
