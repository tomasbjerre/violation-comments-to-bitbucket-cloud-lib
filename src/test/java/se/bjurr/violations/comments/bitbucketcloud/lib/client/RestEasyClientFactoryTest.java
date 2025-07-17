package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import se.bjurr.bitbucketcloud.gen.api.RepositoriesApi;
import se.bjurr.bitbucketcloud.gen.model.*;
import se.bjurr.violations.comments.bitbucketcloud.lib.BitbucketCloudCommentsProvider;
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
    testGetDiffspec();
    testGetCommitsInPR();
    testCreateCommentInPr();
    final String commentId = testGetCommentsInPR();
    System.out.println("commentId: " + commentId);
    testDeleteCommentsInPR(commentId);
  }

  public void testGetDiffspec() {
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
        client.repositoriesWorkspaceRepoSlugDiffstatSpecGet(
            username, repoSlug, spec, ignoreWhitespace);

    assertThat(actual).isNotNull();
    assertThat(actual.getValues()).hasSize(2);
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
    final Integer context = null;
    final String path = null;
    final String actual =
        client.repositoriesWorkspaceRepoSlugDiffSpecGet(
            username, spec, repoSlug, context, path, ignoreWhitespace, false);

    assertThat(actual).isNotNull();

    System.out.println(actual);
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
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommitsGet(
            username, pullRequestId, repoSlug);

    assertThat(actual).isNotNull();
    assertThat(actual.getValues()).hasSize(2);
    assertThat(
            BitbucketCloudCommentsProvider.getOrderedCommits(actual.getValues()).get(0).getHash())
        .isEqualTo("a31d1b70c972c9476346232909a739b0416c4328");
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
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsGet(
            username, repoSlug, pullRequestId);

    assertThat(actual).isNotNull();
    assertThat(actual.getValues()).isNotEmpty();
    return BitbucketCloudCommentsProvider.getOrderedComments(actual.getValues()).get(0).getId()
        + "";
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
        client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsPost(
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
    client.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsCommentIdDelete(
        username, pullRequestId, commentId, repoSlug);
  }
}
