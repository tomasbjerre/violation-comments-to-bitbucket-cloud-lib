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

public class BitbucketCloudWireMockTest {

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
  public void testGetPullRequest() {
    // Given - Based on actual Bitbucket Cloud API response format
    final String pullRequestJson =
        """
        {
          "type": "pullrequest",
          "id": 1,
          "title": "Test PR",
          "state": "OPEN",
          "author": {
            "type": "user",
            "display_name": "Test User",
            "uuid": "{test-uuid}"
          },
          "source": {
            "commit": {
              "hash": "abc123"
            },
            "branch": {
              "name": "feature"
            },
            "repository": {
              "type": "repository",
              "full_name": "testworkspace/testrepo",
              "name": "testrepo",
              "uuid": "{repo-uuid}"
            }
          },
          "destination": {
            "commit": {
              "hash": "def456"
            },
            "branch": {
              "name": "master"
            },
            "repository": {
              "type": "repository",
              "full_name": "testworkspace/testrepo",
              "name": "testrepo",
              "uuid": "{repo-uuid}"
            }
          },
          "created_on": "2023-01-01T12:00:00.000000+00:00",
          "updated_on": "2023-01-01T12:00:00.000000+00:00"
        }
        """;

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(pullRequestJson)));

    // When
    final Pullrequest pullrequest =
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdGet(
            "testworkspace", "testrepo", "1");

    // Then
    assertThat(pullrequest).isNotNull();
    assertThat(pullrequest.getId()).isEqualTo(1);
    assertThat(pullrequest.getTitle()).isEqualTo("Test PR");
    assertThat(pullrequest.getState()).isEqualTo(Pullrequest.StateEnum.OPEN);
    assertThat(pullrequest.getSource().getCommit().getHash()).isEqualTo("abc123");
    assertThat(pullrequest.getDestination().getCommit().getHash()).isEqualTo("def456");

    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1"))
            .withHeader("Authorization", matching("Basic .*")));
  }

  @Test
  public void testGetDiffstat() {
    // Given
    final String diffstatJson =
        """
        {
          "values": [
            {
              "type": "diffstat",
              "status": "modified",
              "lines_removed": 5,
              "lines_added": 10,
              "old": {
                "type": "commit_file",
                "path": "src/main/java/Test.java"
              },
              "new": {
                "type": "commit_file",
                "path": "src/main/java/Test.java"
              }
            },
            {
              "type": "diffstat",
              "status": "added",
              "lines_removed": 0,
              "lines_added": 20,
              "old": null,
              "new": {
                "type": "commit_file",
                "path": "src/main/java/NewFile.java"
              }
            }
          ],
          "page": 1,
          "pagelen": 10,
          "size": 2
        }
        """;

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/diffstat/abc123..def456"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(diffstatJson)));

    // When
    final PaginatedDiffstats diffstats =
        client.repositoriesWorkspaceRepoSlugDiffstatSpecGet(
            "testworkspace", "testrepo", "abc123..def456", null);

    // Then
    assertThat(diffstats).isNotNull();
    assertThat(diffstats.getValues()).hasSize(2);
    assertThat(diffstats.getValues().iterator().next().getStatus())
        .isEqualTo(Diffstat.StatusEnum.MODIFIED);
    assertThat(diffstats.getValues().iterator().next().getNew().getPath())
        .isEqualTo("src/main/java/Test.java");
  }

  @Test
  public void testGetDiff() {
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
  public void testGetComments() {
    // Given
    final String commentsJson =
        """
        {
          "values": [
            {
              "type": "comment",
              "id": 123,
              "created_on": "2023-01-01T12:00:00.000000+00:00",
              "updated_on": "2023-01-01T12:00:00.000000+00:00",
              "content": {
                "raw": "This is a test comment",
                "markup": "markdown",
                "html": "<p>This is a test comment</p>"
              },
              "user": {
                "type": "user",
                "display_name": "Test User",
                "uuid": "{user-uuid}"
              }
            }
          ],
          "page": 1,
          "pagelen": 10,
          "size": 1
        }
        """;

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(commentsJson)));

    // When
    final PaginatedPullrequestComments comments =
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsGet(
            "testworkspace", "testrepo", "1");

    // Then
    assertThat(comments).isNotNull();
    assertThat(comments.getValues()).hasSize(1);
    assertThat(comments.getValues().iterator().next().getId()).isEqualTo(123);
    assertThat(comments.getValues().iterator().next().getContent().getRaw())
        .isEqualTo("This is a test comment");
  }

  @Test
  public void testCreateComment() {
    // Given
    final String createdCommentJson =
        """
        {
          "type": "comment",
          "id": 456,
          "created_on": "2023-01-01T12:00:00.000000+00:00",
          "updated_on": "2023-01-01T12:00:00.000000+00:00",
          "content": {
            "raw": "New comment",
            "markup": "markdown",
            "html": "<p>New comment</p>"
          }
        }
        """;

    wireMockServer.stubFor(
        post(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(createdCommentJson)));

    // When
    final CommentContent content = new CommentContent();
    content.setRaw("New comment");
    final Comment comment = new Comment();
    comment.setContent(content);

    final Comment created =
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsPost(
            "testworkspace", "testrepo", "1", comment);

    // Then
    assertThat(created).isNotNull();
    assertThat(created.getId()).isEqualTo(456);
    assertThat(created.getContent().getRaw()).isEqualTo("New comment");

    wireMockServer.verify(
        postRequestedFor(
                urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .withHeader("Content-Type", containing("application/json"))
            .withRequestBody(containing("New comment")));
  }

  @Test
  public void testGetActivity() {
    // Given
    final String activityJson =
        """
        {
          "values": [
            {
              "comment": {
                "type": "comment",
                "id": 789,
                "created_on": "2023-01-01T12:00:00.000000+00:00",
                "updated_on": "2023-01-01T12:00:00.000000+00:00",
                "content": {
                  "raw": "Activity comment",
                  "markup": "markdown"
                }
              }
            }
          ],
          "page": 1,
          "pagelen": 10,
          "size": 1
        }
        """;

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/activity"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(activityJson)));

    // When
    final PaginatedActivities activities =
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdActivityGet(
            "testworkspace", "testrepo", "1");

    // Then
    assertThat(activities).isNotNull();
    assertThat(activities.getValues()).hasSize(1);
    assertThat(activities.getValues().iterator().next().getComment()).isNotNull();
    assertThat(activities.getValues().iterator().next().getComment().getId()).isEqualTo(789);
    assertThat(activities.getValues().iterator().next().getComment().getContent().getRaw())
        .isEqualTo("Activity comment");
  }

  @Test
  public void testDeleteComment() {
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
  public void testApiTokenAuthentication() {
    // Given
    api.withApiToken("test-api-token");
    api.withUsername(null);
    api.withPassword(null);
    client = RestEasyClientFactory.create(RepositoriesApi.class, api, "http://localhost:8089");

    final String pullRequestJson =
        """
        {
          "type": "pullrequest",
          "id": 1,
          "title": "Test PR",
          "state": "OPEN",
          "author": {
            "type": "user",
            "display_name": "Test User",
            "uuid": "{test-uuid}"
          },
          "source": {
            "commit": {
              "hash": "abc123"
            },
            "repository": {
              "type": "repository",
              "name": "testrepo"
            }
          },
          "destination": {
            "commit": {
              "hash": "def456"
            },
            "repository": {
              "type": "repository",
              "name": "testrepo"
            }
          },
          "created_on": "2023-01-01T12:00:00.000000+00:00",
          "updated_on": "2023-01-01T12:00:00.000000+00:00"
        }
        """;

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(pullRequestJson)));

    // When
    final Pullrequest pullrequest =
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdGet(
            "testworkspace", "testrepo", "1");

    // Then
    assertThat(pullrequest).isNotNull();
    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1"))
            .withHeader("Authorization", equalTo("Bearer test-api-token")));
  }

  @Test
  public void testGetCommitsInPullRequest() {
    // Given
    final String commitsJson =
        """
        {
          "values": [
            {
              "type": "commit",
              "hash": "a31d1b70c972c9476346232909a739b0416c4328",
              "date": "2023-01-01T10:00:00+00:00",
              "message": "First commit",
              "author": {
                "type": "author",
                "raw": "Test User <test@example.com>"
              }
            },
            {
              "type": "commit",
              "hash": "b42e2c81d083d0587457343010b850c1527d5439",
              "date": "2023-01-01T11:00:00+00:00",
              "message": "Second commit",
              "author": {
                "type": "author",
                "raw": "Test User <test@example.com>"
              }
            }
          ],
          "page": 1,
          "pagelen": 10,
          "size": 2
        }
        """;

    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/commits"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(commitsJson)));

    // When
    final PaginatedPullrequestsCommits commits =
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommitsGet(
            "testworkspace", "1", "testrepo");

    // Then
    assertThat(commits).isNotNull();
    assertThat(commits.getValues()).hasSize(2);
    assertThat(commits.getValues().iterator().next().getHash())
        .isEqualTo("a31d1b70c972c9476346232909a739b0416c4328");
  }
}
