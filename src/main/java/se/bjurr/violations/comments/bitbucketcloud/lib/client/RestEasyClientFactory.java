package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import javax.ws.rs.core.UriBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
import se.bjurr.violations.comments.bitbucketcloud.lib.ViolationCommentsToBitbucketCloudApi;

public class RestEasyClientFactory {
  public static <T> T create(final Class<T> clazz, final ViolationCommentsToBitbucketCloudApi api) {
    final String path = "https://api.bitbucket.org/2.0";

    final ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.setSerializationInclusion(Include.NON_DEFAULT);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    final JacksonJaxbJsonProvider jaxbJsonProvider =
        new JacksonJaxbJsonProvider(mapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);
    jaxbJsonProvider.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    jaxbJsonProvider.enable(SerializationFeature.INDENT_OUTPUT);
    final ResteasyClient client =
        new ResteasyClientBuilderImpl() //
            .connectTimeout(10, SECONDS) //
            .readTimeout(10, SECONDS) //
            .connectionCheckoutTimeout(10, SECONDS) //
            .register(new RequestLoggingFilter(api.getViolationsLogger())) //
            .register(new ResponseLoggingFilter(api.getViolationsLogger())) //
            .register(new BasicAuthentication(api.getUsername(), api.getPassword())) //
            .register(jaxbJsonProvider) //
            .register(new DefaultTextPlain()) //
            .build();

    final ResteasyWebTarget target = client.target(UriBuilder.fromPath(path));
    final T proxy = target.proxy(clazz);
    return proxy;
  }
}
