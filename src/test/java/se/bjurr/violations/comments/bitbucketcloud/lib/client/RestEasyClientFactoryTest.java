package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import se.bjurr.bitbucketcloud.gen.api.RepositoriesApi;
import se.bjurr.bitbucketcloud.gen.model.Comment;
import se.bjurr.bitbucketcloud.gen.model.CommentContent;
import se.bjurr.bitbucketcloud.gen.model.PaginatedDiffstats;
import se.bjurr.bitbucketcloud.gen.model.PaginatedPullrequestComments;
import se.bjurr.bitbucketcloud.gen.model.PaginatedPullrequestsCommits;
import se.bjurr.violations.comments.bitbucketcloud.lib.ViolationCommentsToBitbucketCloudApi;

public class RestEasyClientFactoryTest {

  private static final String COMMENT_CONTENT_STR = "asdasd";
  private String password;

  @Before
  public void before() throws IOException {
    final Path path = Paths.get("/home/bjerre/bitbucket-cloud-password.txt");
    if (!path.toFile().exists()) {
      return;
    }
    this.password = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
  }

  // @Test
  public void doTest() {
    testGetDiff();
    testGetCommitsInPR();
    testCreateCommentInPr();
    final String commentId = testGetCommentsInPR();
    System.out.println("commentId: " + commentId);
    testDeleteCommentsInPR(commentId);
  }

  public void testGetDiff() {
    if (password == null) {
      return;
    }
    final ViolationCommentsToBitbucketCloudApi api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("tomasbjerre");
    api.withPassword(password);

    final RepositoriesApi client = RestEasyClientFactory.create(RepositoriesApi.class, api);
    final String username = "tomasbjerre";
    final String repoSlug = "violations-test";

    final String spec = "7e23c9a98019..master";
    final Boolean ignoreWhitespace = true;
    final PaginatedDiffstats actual =
        client.repositoriesUsernameRepoSlugDiffstatSpecGet(
            username, repoSlug, spec, ignoreWhitespace);

    assertThat(actual).isNotNull();
    assertThat(actual.getValues()).hasSize(2);
  }

  public void testGetCommitsInPR() {
    if (password == null) {
      return;
    }
    final ViolationCommentsToBitbucketCloudApi api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("tomasbjerre");
    api.withPassword(password);

    final RepositoriesApi client = RestEasyClientFactory.create(RepositoriesApi.class, api);
    final String username = "tomasbjerre";
    final String repoSlug = "violations-test";
    final String pullRequestId = "1";
    final PaginatedPullrequestsCommits actual =
        client.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommitsGet(
            username, pullRequestId, repoSlug);

    assertThat(actual).isNotNull();
    assertThat(actual.getValues()).hasSize(2);
    assertThat(actual.getValues().get(0).getHash())
        .isEqualTo("7e23c9a980197fe49fae67fb23687c857ff42f86");
  }

  public String testGetCommentsInPR() {
    if (password == null) {
      return "";
    }
    final ViolationCommentsToBitbucketCloudApi api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("tomasbjerre");
    api.withPassword(password);

    final RepositoriesApi client = RestEasyClientFactory.create(RepositoriesApi.class, api);
    final String username = "tomasbjerre";
    final String repoSlug = "violations-test";
    final String pullRequestId = "1";
    final PaginatedPullrequestComments actual =
        client.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommentsGet(
            username, repoSlug, pullRequestId);

    assertThat(actual).isNotNull();
    assertThat(actual.getValues()).isNotEmpty();
    return actual.getValues().get(0).getId() + "";
  }

  private void testCreateCommentInPr() {
    if (password == null) {
      return;
    }
    final ViolationCommentsToBitbucketCloudApi api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("tomasbjerre");
    api.withPassword(password);

    final RepositoriesApi client = RestEasyClientFactory.create(RepositoriesApi.class, api);
    final String username = "tomasbjerre";
    final String repoSlug = "violations-test";
    final String pullRequestId = "1";
    final CommentContent content = new CommentContent();
    content.setRaw(COMMENT_CONTENT_STR);
    final Comment comment = new Comment();
    comment.setContent(content);
    final Comment created =
        client.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommentsPost(
            username, repoSlug, pullRequestId, comment);
    System.out.println("created: " + created);
  }

  public void testDeleteCommentsInPR(final String commentId) {
    if (password == null) {
      return;
    }
    final ViolationCommentsToBitbucketCloudApi api = new ViolationCommentsToBitbucketCloudApi();
    api.withUsername("tomasbjerre");
    api.withPassword(password);

    final RepositoriesApi client = RestEasyClientFactory.create(RepositoriesApi.class, api);
    final String username = "tomasbjerre";
    final String repoSlug = "violations-test";
    final String pullRequestId = "1";
    client.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommentsCommentIdDelete(
        username, pullRequestId, commentId, repoSlug);
  }
}
