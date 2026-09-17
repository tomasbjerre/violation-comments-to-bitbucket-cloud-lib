package se.bjurr.violations.comments.bitbucketcloud.lib;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.bjurr.violations.comments.lib.model.Comment;

public class BitbucketCloudCommentsProviderTest {

  private WireMockServer wireMockServer;
  private BitbucketCloudCommentsProvider provider;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(8089);
    wireMockServer.start();

    final ViolationCommentsToBitbucketCloudApi api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("testuser");
    api.withPassword("testpass");
    api.withWorkspace("testworkspace");
    api.withRepositorySlug("testrepo");
    api.withPullRequestId("1");

    provider = new BitbucketCloudCommentsProvider(api, "http://localhost:8089");
  }

  @AfterEach
  public void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  private List<Comment> commentWithId(final String id) {
    return Collections.singletonList(
        new Comment(id, "some comment", null, Collections.emptyList()));
  }

  @Test
  public void testRemoveComments_ResolvesInsteadOfDeleting() {
    wireMockServer.stubFor(
        post(urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/123/resolve"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody("{\"type\": \"resolution\"}")));

    provider.removeComments(commentWithId("123"));

    wireMockServer.verify(
        postRequestedFor(
            urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/123/resolve")));
    assertThat(wireMockServer.getAllServeEvents()).hasSize(1);
  }

  @Test
  public void testRemoveComments_AlreadyResolved_IsNotAnError() {
    // A comment resolved on a previous run is still returned by getComments(), so a later
    // run can be asked to remove the same comment again and hit 409 - that's not a failure,
    // there's nothing left to do.
    wireMockServer.stubFor(
        post(urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/123/resolve"))
            .willReturn(
                aResponse()
                    .withStatus(409)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody("{\"type\": \"error\"}")));

    provider.removeComments(commentWithId("123"));

    wireMockServer.verify(
        0,
        deleteRequestedFor(
            urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments/123")));
  }

  @Test
  public void testRemoveComments_NotResolvable_FallsBackToDelete() {
    // 403 - "the provided comment is not a top-level comment" per Bitbucket Cloud's docs for
    // this endpoint. Whatever the reason resolving fails, the comment should still get cleaned
    // up rather than the whole run crashing.
    wireMockServer.stubFor(
        post(urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/123/resolve"))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody("{\"type\": \"error\"}")));
    wireMockServer.stubFor(
        com.github.tomakehurst.wiremock.client.WireMock.delete(
                urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments/123"))
            .willReturn(aResponse().withStatus(204)));

    provider.removeComments(commentWithId("123"));

    wireMockServer.verify(
        deleteRequestedFor(
            urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments/123")));
  }
}
