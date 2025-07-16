package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import se.bjurr.violations.lib.ViolationsLogger;

public class ResponseLoggingFilter implements ClientResponseFilter {
  private final ViolationsLogger logger;

  public ResponseLoggingFilter(final ViolationsLogger logger) {
    this.logger = logger;
  }

  @Override
  public void filter(
      final ClientRequestContext requestContext, final ClientResponseContext responseContext)
      throws IOException {
    final int status = responseContext.getStatus();
    final String statusMsg = responseContext.getStatusInfo().getReasonPhrase();
    final String msg = status + " " + statusMsg;
    if (status >= 200 && status <= 299) {
      this.logger.log(Level.FINE, "\n<< " + msg + "\n\n");
    } else {
      String entityString = null;
      try (var br =
          new BufferedReader(
              new InputStreamReader(responseContext.getEntityStream(), StandardCharsets.UTF_8))) {
        entityString = br.lines().collect(Collectors.joining("\n"));
      }
      this.logger.log(Level.SEVERE, "\n<< " + msg + " " + entityString + "\n\n");
    }
  }
}
