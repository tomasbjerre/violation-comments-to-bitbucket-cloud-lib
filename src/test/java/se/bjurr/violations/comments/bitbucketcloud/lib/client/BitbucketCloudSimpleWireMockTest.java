package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.bjurr.bitbucketcloud.gen.api.RepositoriesApi;
import se.bjurr.bitbucketcloud.gen.model.*;
import se.bjurr.violations.comments.bitbucketcloud.lib.ViolationCommentsToBitbucketCloudApi;

/**
 * Simplified WireMock tests that verify the JSON parsing fix for RESTEasy client. These tests
 * ensure that the Jackson JSON provider is properly registered and can parse Bitbucket Cloud API
 * responses.
 */
public class BitbucketCloudSimpleWireMockTest {

  private WireMockServer wireMockServer;
  private ViolationCommentsToBitbucketCloudApi api;
  private RepositoriesApi client;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(8089);
    wireMockServer.start();

    api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("testuser");
    api.withPassword("testpass");
    api.withWorkspace("testworkspace");
    api.withRepositorySlug("testrepo");
    api.withPullRequestId("1");

    client = RestEasyClientFactory.create(RepositoriesApi.class, api, "http://localhost:8089");
  }

  @AfterEach
  public void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  public void testGetDiff_VerifiesTextResponseParsing() {
    // Given
    final String diffContent =
        """
        diff --git a/src/main/java/Test.java b/src/main/java/Test.java
        index 1234567..abcdefg 100644
        --- a/src/main/java/Test.java
        +++ b/src/main/java/Test.java
        @@ -1,5 +1,10 @@
         public class Test {
        -  private String oldField;
        +  private String newField;
        +  private int count;
         }
        """;

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/diff/abc123..def456"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody(diffContent)));

    // When
    final String diff =
        client.repositoriesWorkspaceRepoSlugDiffSpecGet(
            "testworkspace", "abc123..def456", "testrepo", null, null, null, false);

    // Then
    assertThat(diff).isNotNull();
    assertThat(diff).contains("diff --git a/src/main/java/Test.java");
    assertThat(diff).contains("+  private String newField;");
  }

  @Test
  public void testDeleteComment_Verifies204Response() {
    // Given
    wireMockServer.stubFor(
        delete(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments/123"))
            .willReturn(aResponse().withStatus(204)));

    // When
    client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsCommentIdDelete(
        "testworkspace", "1", "123", "testrepo");

    // Then
    wireMockServer.verify(
        deleteRequestedFor(
            urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments/123")));
  }

  @Test
  public void testBasicAuthenticationHeader() {
    // Given
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/diff/test..master"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("diff content")));

    // When
    client.repositoriesWorkspaceRepoSlugDiffSpecGet(
        "testworkspace", "test..master", "testrepo", null, null, null, false);

    // Then
    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/repositories/testworkspace/testrepo/diff/test..master"))
            .withHeader("Authorization", matching("Basic .*")));
  }

  @Test
  public void testApiTokenAuthentication() {
    // Given
    api.withApiToken("test-api-token");
    api.withUsername(null);
    api.withPassword(null);
    client = RestEasyClientFactory.create(RepositoriesApi.class, api, "http://localhost:8089");

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/diff/test..master"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("diff content")));

    // When
    client.repositoriesWorkspaceRepoSlugDiffSpecGet(
        "testworkspace", "test..master", "testrepo", null, null, null, false);

    // Then
    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/repositories/testworkspace/testrepo/diff/test..master"))
            .withHeader("Authorization", equalTo("Bearer test-api-token")));
  }
}
