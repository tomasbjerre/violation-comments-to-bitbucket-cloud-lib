package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import java.io.IOException;
import java.util.logging.Level;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import se.bjurr.violations.comments.lib.ViolationsLogger;

public class RequestLoggingFilter implements ClientRequestFilter {
  private final ViolationsLogger logger;

  public RequestLoggingFilter(final ViolationsLogger logger) {
    this.logger = logger;
  }

  @Override
  public void filter(final ClientRequestContext requestContext) throws IOException {
    final String path = requestContext.getUri().getPath();
    final String method = requestContext.getMethod();
    final String msg =
        "\n>> "
            + method
            + " "
            + path
            + " "
            + (requestContext.getEntity() == null ? "" : "\n" + requestContext.getEntity())
            + "\n\n";
    logger.log(Level.FINE, msg);
  }
}
