package se.bjurr.violations.comments.bitbucketcloud.lib;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static se.bjurr.violations.comments.bitbucketcloud.lib.BitbucketCloudCommentsProvider.SPECIFIC_TASK_ID;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.bjurr.violations.comments.lib.model.ChangedFile;
import se.bjurr.violations.comments.lib.model.Comment;

/**
 * These tests replay HTTP traffic captured from a real pull request on Bitbucket Cloud
 * (https://bitbucket.org/tomasbjerre/violations-test/pull-requests/1), so the response bodies are
 * the exact JSON Bitbucket returns rather than hand-written approximations. This matters most for
 * the resolve-comment flow added for comment resolution: the success, "already resolved" (409) and
 * "not a top-level comment" (403) bodies below are all real captures, not guesses at Bitbucket's
 * error format.
 */
public class BitbucketCloudRealPullRequestWireMockTest {

  private static final String DIFF_SPEC = "a31d1b70c972..b563d040b08e";
  private static final String MY_CLASS_PATH =
      "src/main/java/se/bjurr/violations/lib/example/MyClass.java";

  private WireMockServer wireMockServer;
  private BitbucketCloudCommentsProvider provider;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    wireMockServer.start();

    final ViolationCommentsToBitbucketCloudApi api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("testuser");
    api.withPassword("testpass");
    api.withWorkspace("testworkspace");
    api.withRepositorySlug("testrepo");
    api.withPullRequestId("1");
    api.withShouldCommentOnlyChangedContent(true);

    provider = new BitbucketCloudCommentsProvider(api, wireMockServer.baseUrl());
  }

  @AfterEach
  public void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  private static String resource(final String name) {
    final String path = "realpr/" + name;
    try (InputStream is =
        BitbucketCloudRealPullRequestWireMockTest.class.getResourceAsStream(path)) {
      if (is == null) {
        throw new IllegalStateException("Missing test resource: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Stubs an empty task list, for tests where no comment in play has a task attached. */
  private void stubNoTasks() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody("{\"pagelen\": 100, \"size\": 0, \"page\": 1, \"values\": []}")));
  }

  private void stubPullRequestAndDiffstat() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("pullrequest.json"))));
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/diffstat/" + DIFF_SPEC))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("diffstat.json"))));
  }

  @Test
  public void testGetFiles_ParsesRealPullRequestAndDiffstat() {
    stubPullRequestAndDiffstat();

    final List<ChangedFile> files = provider.getFiles();

    // The real diffstat also lists gradlew.bat as removed ("new": null); getFiles() must
    // exclude it since a removed file can't be commented on.
    assertThat(files)
        .extracting(ChangedFile::getFilename)
        .containsExactlyInAnyOrder(
            MY_CLASS_PATH, "src/main/java/se/bjurr/violations/lib/example/OtherClass.java");
  }

  @Test
  public void testGetComments_ParsesRealActivityAndComments() {
    stubNoTasks();
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/activity"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("activity.json"))));
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("comments.json"))));

    final List<Comment> comments = provider.getComments();

    // getComments() concatenates activity comments and PR comments without de-duplicating, so
    // the one real comment surfaced via /activity and the three real comments surfaced via
    // /comments all show up: an inline Checkstyle violation, a top-level accumulated-violations
    // comment, a comment deleted by a previous run (empty content), and a plain human comment.
    assertThat(comments).hasSize(4);
    assertThat(comments)
        .extracting(Comment::getIdentifier)
        .containsExactlyInAnyOrder("216447514", "105331691", "138478756", "104266492");

    final Comment checkstyleComment =
        comments.stream().filter(c -> c.getIdentifier().equals("105331691")).findFirst().get();
    assertThat(checkstyleComment.getContent())
        .contains("Checkstyle")
        .contains("EmptyBlockCheck")
        .contains("Must have at least one statement.");

    final Comment accumulatedComment =
        comments.stream().filter(c -> c.getIdentifier().equals("138478756")).findFirst().get();
    assertThat(accumulatedComment.getContent()).contains("Found 3 violations");

    final Comment deletedComment =
        comments.stream().filter(c -> c.getIdentifier().equals("104266492")).findFirst().get();
    assertThat(deletedComment.getContent()).isEmpty();
  }

  @Test
  public void testShouldComment_UsesRealDiffToDetectChangedLines() {
    stubPullRequestAndDiffstat();
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/diff/" + DIFF_SPEC))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody(resource("diff.txt"))));

    final ChangedFile myClass = new ChangedFile(MY_CLASS_PATH, Collections.emptyList());

    // The real diff's hunk header is "@@ -9,6 +9,8 @@", so the new file's lines 9-16 (the
    // hunk's context plus the two added lines) are considered "in diff", and line 3 - part of
    // the class declaration, well before the hunk - is not.
    assertThat(provider.shouldComment(myClass, 12)).isTrue();
    assertThat(provider.shouldComment(myClass, 3)).isFalse();
  }

  @Test
  public void testCreateComment_ParsesRealBitbucketResponseShape() {
    // The real 201 response is far richer than a hand-written fixture: nested rendered content,
    // a full user object with account_id/uuid, and an embedded pullrequest object. This exercises
    // that the Jackson 3 mapper still deserializes all of that without throwing.
    wireMockServer.stubFor(
        post(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("create-comment-response.json"))));

    final String violationText =
        "**Reporter**: Checkstyle\n"
            + "**Rule**: com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck\n"
            + "**Severity**: INFO\n"
            + "**File**: "
            + MY_CLASS_PATH
            + " L9\n\n"
            + "Must have at least one statement.";

    provider.createComment(violationText);

    wireMockServer.verify(
        postRequestedFor(
                urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .withRequestBody(containing("Must have at least one statement.")));
  }

  @Test
  public void testCreateComment_DoesNotCreateATaskByDefault() {
    wireMockServer.stubFor(
        post(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("create-task-anchor-comment-response.json"))));

    provider.createComment("Some violation");

    wireMockServer.verify(
        0,
        postRequestedFor(
            urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks")));
  }

  @Test
  public void testCreateComment_CreatesATaskWhenConfiguredTo() {
    // Real captures from posting a comment, then a task anchored to it, on the real PR.
    wireMockServer.stubFor(
        post(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("create-task-anchor-comment-response.json"))));
    wireMockServer.stubFor(
        post(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("create-task-response.json"))));

    final ViolationCommentsToBitbucketCloudApi api =
        new ViolationCommentsToBitbucketCloudApi()
            .withUsername("testuser")
            .withPassword("testpass")
            .withWorkspace("testworkspace")
            .withRepositorySlug("testrepo")
            .withPullRequestId("1")
            .withCreateCommentTasks(true);
    final BitbucketCloudCommentsProvider taskProvider =
        new BitbucketCloudCommentsProvider(api, wireMockServer.baseUrl());

    taskProvider.createComment("[violations-test] task-cascade probe comment");

    // The real anchor comment's id (865602458), as the created task must reference it.
    wireMockServer.verify(
        postRequestedFor(
                urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks"))
            .withRequestBody(containing("865602458"))
            .withRequestBody(containing("task-cascade probe comment")));
  }

  @Test
  public void testCreateSingleFileComment_CreatesATaskWhenConfiguredTo() {
    // Real captures from posting an inline diff comment, then a task anchored to it.
    wireMockServer.stubFor(
        post(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("create-inline-task-anchor-comment-response.json"))));
    wireMockServer.stubFor(
        post(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("create-inline-task-response.json"))));

    final ViolationCommentsToBitbucketCloudApi api =
        new ViolationCommentsToBitbucketCloudApi()
            .withUsername("testuser")
            .withPassword("testpass")
            .withWorkspace("testworkspace")
            .withRepositorySlug("testrepo")
            .withPullRequestId("1")
            .withCreateCommentTasks(true);
    final BitbucketCloudCommentsProvider taskProvider =
        new BitbucketCloudCommentsProvider(api, wireMockServer.baseUrl());

    final ChangedFile myClass = new ChangedFile(MY_CLASS_PATH, Collections.emptyList());
    taskProvider.createSingleFileComment(myClass, 5, "[violations-test] inline task-cascade probe");

    // The real inline anchor comment's id (865602816).
    wireMockServer.verify(
        postRequestedFor(
                urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks"))
            .withRequestBody(containing("865602816")));
  }

  @Test
  public void testGetComments_MarksCommentsThatHaveATaskAttached() {
    // Real capture: the tasks list for the PR, with one task on each of two real comments -
    // one already RESOLVED, one still UNRESOLVED. Enrichment only cares that a task exists, not
    // its state, since removeComments() unconditionally resolves whatever task it finds.
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("tasks-list-response.json"))));
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/activity"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody("{\"pagelen\": 100, \"size\": 0, \"page\": 1, \"values\": []}")));
    wireMockServer.stubFor(
        get(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/comments"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("comments-with-tasks.json"))));

    final List<Comment> comments = provider.getComments();

    assertThat(comments).hasSize(2);
    final Comment topLevel =
        comments.stream().filter(c -> c.getIdentifier().equals("865602458")).findFirst().get();
    assertThat(topLevel.getSpecifics().get(SPECIFIC_TASK_ID)).isEqualTo("72305227");
    final Comment inline =
        comments.stream().filter(c -> c.getIdentifier().equals("865602816")).findFirst().get();
    assertThat(inline.getSpecifics().get(SPECIFIC_TASK_ID)).isEqualTo("72305281");
  }

  @Test
  public void testRemoveComments_ResolvesTheTaskInsteadOfTheCommentWhenOneIsAttached() {
    wireMockServer.stubFor(
        put(urlPathEqualTo("/repositories/testworkspace/testrepo/pullrequests/1/tasks/72305227"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("resolve-task-response.json"))));

    provider.removeComments(
        Collections.singletonList(
            new Comment("865602458", "some comment", null, List.of("72305227"))));

    wireMockServer.verify(
        putRequestedFor(
                urlPathEqualTo(
                    "/repositories/testworkspace/testrepo/pullrequests/1/tasks/72305227"))
            .withRequestBody(containing("RESOLVED")));
    wireMockServer.verify(
        0,
        postRequestedFor(
            urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/865602458/resolve")));
    wireMockServer.verify(
        0,
        deleteRequestedFor(
            urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/865602458")));
  }

  @Test
  public void testRemoveComments_ResolvesUsingRealBitbucketResponse() {
    // Real 200 response body: {"type": "comment_resolution", "user": {...}, "created_on": ...}
    wireMockServer.stubFor(
        post(urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/138478756/resolve"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("resolve-success.json"))));

    provider.removeComments(
        Collections.singletonList(new Comment("138478756", "some comment", null, List.of(""))));

    wireMockServer.verify(
        postRequestedFor(
            urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/138478756/resolve")));
    wireMockServer.verify(
        0,
        deleteRequestedFor(
            urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/138478756")));
  }

  @Test
  public void testRemoveComments_AlreadyResolved_RealConflictResponseIsNotAnError() {
    // Real 409 body: {"type": "error", "error": {"message": "Comment has already been
    // resolved."}} - returned when a comment resolved by a previous run is asked to be resolved
    // again.
    wireMockServer.stubFor(
        post(urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/138478756/resolve"))
            .willReturn(
                aResponse()
                    .withStatus(409)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("resolve-conflict.json"))));

    provider.removeComments(
        Collections.singletonList(new Comment("138478756", "some comment", null, List.of(""))));

    wireMockServer.verify(
        0,
        deleteRequestedFor(
            urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/138478756")));
  }

  @Test
  public void testRemoveComments_NotTopLevelComment_FallsBackToRealDelete() {
    // Real 403 body when resolving a reply comment (only top-level comments can be resolved):
    // {"type": "error", "error": {"message": "Comment is not a top-level comment"}}
    wireMockServer.stubFor(
        post(urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/105331691/resolve"))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/json;charset=utf-8")
                    .withBody(resource("resolve-not-top-level.json"))));
    // Real DELETE response: 204 with an empty body.
    wireMockServer.stubFor(
        delete(
                urlPathEqualTo(
                    "/repositories/testworkspace/testrepo/pullrequests/1/comments/105331691"))
            .willReturn(aResponse().withStatus(204)));

    provider.removeComments(
        Collections.singletonList(new Comment("105331691", "some comment", null, List.of(""))));

    wireMockServer.verify(
        deleteRequestedFor(
            urlPathEqualTo(
                "/repositories/testworkspace/testrepo/pullrequests/1/comments/105331691")));
  }
}
