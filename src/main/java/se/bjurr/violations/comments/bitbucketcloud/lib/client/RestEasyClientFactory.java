package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import se.bjurr.violations.comments.bitbucketcloud.lib.ViolationCommentsToBitbucketCloudApi;

public class RestEasyClientFactory {
  public static <T> T create(final Class<T> clazz, final ViolationCommentsToBitbucketCloudApi api) {
    return create(clazz, api, "https://api.bitbucket.org/2.0");
  }

  public static <T> T create(
      final Class<T> clazz, final ViolationCommentsToBitbucketCloudApi api, final String baseUrl) {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.setSerializationInclusion(Include.NON_DEFAULT);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    final JacksonJsonProvider jsonProvider = new JacksonJsonProvider(mapper);

    final ResteasyClient client =
        new ResteasyClientBuilderImpl() //
            .connectTimeout(10, SECONDS) //
            .readTimeout(10, SECONDS) //
            .connectionCheckoutTimeout(10, SECONDS) //
            .register(new RequestLoggingFilter(api.getViolationsLogger())) //
            .register(new ResponseLoggingFilter(api.getViolationsLogger())) //
            .register(
                api.getApiToken() != null
                    ? new ApiTokenAuthentication(api.getApiToken())
                    : new BasicAuthentication(api.getUsername(), api.getPassword())) //
            .register(jsonProvider) //
            .register(new StringTextStar()) //
            .build();

    final ResteasyWebTarget target = client.target(UriBuilder.fromPath(baseUrl));
    final T proxy = target.proxy(clazz);
    return proxy;
  }
}
