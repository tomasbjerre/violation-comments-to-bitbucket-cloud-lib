package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.internal.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import se.bjurr.bitbucketcloud.gen.model.Comment;
import se.bjurr.bitbucketcloud.gen.model.PullrequestCommentTask;
import se.bjurr.violations.comments.bitbucketcloud.lib.ViolationCommentsToBitbucketCloudApi;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

public class RestEasyClientFactory {
  public static <T> T create(final Class<T> clazz, final ViolationCommentsToBitbucketCloudApi api) {
    return create(clazz, api, "https://api.bitbucket.org/2.0");
  }

  public static <T> T create(
      final Class<T> clazz, final ViolationCommentsToBitbucketCloudApi api, final String baseUrl) {
    // Jackson 3: java.time (de)serialization is built into jackson-databind, no separate
    // JavaTimeModule needed; ObjectMapper is immutable, so all configuration happens on the
    // builder rather than via mutator calls.
    final JsonMapper mapper =
        JsonMapper.builder()
            .changeDefaultPropertyInclusion(v -> v.withValueInclusion(Include.NON_DEFAULT))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
            .disable(DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY)
            // The Bitbucket Cloud API returns enum values (e.g. diffstat status "type changed")
            // that aren't in the generated client's enums, since even Atlassian's own published
            // spec doesn't document them. Read as null rather than throwing, since callers here
            // never rely on getting every enum constant resolved.
            .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            // See PullrequestCommentTaskMixin: the comment a task is anchored to is returned as
            // a minimal, type-less reference, which the generated Comment model - polymorphic by
            // default - can't otherwise resolve.
            .addMixIn(PullrequestCommentTask.class, PullrequestCommentTaskMixin.class)
            .addMixIn(Comment.class, PullrequestCommentTaskMixin.CommentMixin.class)
            .build();

    final JacksonJakartaJsonProvider jsonProvider = new JacksonJakartaJsonProvider(mapper);

    final ResteasyClient client = // NOPMD must stay open to back the returned proxy
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
