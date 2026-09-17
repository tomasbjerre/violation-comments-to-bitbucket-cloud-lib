package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
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
      // Read the entity stream fully, then replace it with a fresh stream over the same bytes -
      // consuming (and, worse, closing, as a try-with-resources reader would) the original
      // stream here leaves nothing for the client proxy to read afterwards, so it can't build
      // the exception/entity it hands back to the caller and throws a confusing
      // IllegalStateException("Response is closed") instead of the real error.
      final byte[] entityBytes = responseContext.getEntityStream().readAllBytes();
      final String entityString = new String(entityBytes, StandardCharsets.UTF_8);
      this.logger.log(Level.SEVERE, "\n<< " + msg + " " + entityString + "\n\n");
      responseContext.setEntityStream(new ByteArrayInputStream(entityBytes));
    }
  }
}
