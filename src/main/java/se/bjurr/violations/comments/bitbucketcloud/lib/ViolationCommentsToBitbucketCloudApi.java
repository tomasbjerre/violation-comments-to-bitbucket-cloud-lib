package se.bjurr.violations.comments.bitbucketcloud.lib;

import static se.bjurr.violations.comments.lib.CommentsCreator.createComments;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.slf4j.LoggerFactory;
import se.bjurr.violations.comments.lib.CommentsProvider;
import se.bjurr.violations.comments.lib.ViolationsLogger;
import se.bjurr.violations.lib.model.Violation;

public class ViolationCommentsToBitbucketCloudApi {
  private ViolationsLogger violationsLogger =
      new ViolationsLogger() {
        @Override
        public void log(final Level level, final String string, final Throwable t) {
          final StringWriter sw = new StringWriter();
          t.printStackTrace(new PrintWriter(sw));
          LoggerFactory.getLogger(ViolationCommentsToBitbucketCloudApi.class)
              .info(level + " " + string + " " + sw.toString());
        }

        @Override
        public void log(final Level level, final String string) {
          LoggerFactory.getLogger(ViolationCommentsToBitbucketCloudApi.class)
              .info(level + " " + string);
        }
      };

  private String username;
  /**
   * Note that you can create application password here:
   * https://bitbucket.org/account/user/tomasbjerre/app-passwords
   */
  private String password;

  private String workspace;
  private String repositorySlug;
  private String pullRequestId;
  private int commentOnlyChangedContentContext;
  private boolean createCommentWithAllSingleFileComments;
  private boolean shouldCreateSingleFileComment;
  private boolean shouldKeepOldComments;
  private String commentTemplate;
  private Integer maxCommentSize;
  private Integer maxNumberOfViolations;
  private List<Violation> violations;

  public ViolationCommentsToBitbucketCloudApi() {}

  public static ViolationCommentsToBitbucketCloudApi violationCommentsToBitbucketCloudApi() {
    return new ViolationCommentsToBitbucketCloudApi();
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getWorkspace() {
    return workspace;
  }

  public String getRepositorySlug() {
    return repositorySlug;
  }

  public String getPullRequestId() {
    return pullRequestId;
  }

  public int getCommentOnlyChangedContentContext() {
    return commentOnlyChangedContentContext;
  }

  public boolean shouldCreateCommentWithAllSingleFileComments() {
    return createCommentWithAllSingleFileComments;
  }

  public boolean shouldCreateSingleFileComment() {
    return shouldCreateSingleFileComment;
  }

  public boolean shouldKeepOldComments() {
    return shouldKeepOldComments;
  }

  public Optional<String> findCommentTemplate() {
    return Optional.ofNullable(commentTemplate);
  }

  public Integer getMaxCommentSize() {
    return maxCommentSize;
  }

  public Integer getMaxNumberOfViolations() {
    return maxNumberOfViolations;
  }

  public ViolationsLogger getViolationsLogger() {
    return violationsLogger;
  }

  public ViolationCommentsToBitbucketCloudApi withPassword(final String password) {
    this.password = password;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withUsername(final String username) {
    this.username = username;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withViolationsLogger(
      final ViolationsLogger violationsLogger) {
    this.violationsLogger = violationsLogger;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withViolations(final List<Violation> violations) {
    this.violations = violations;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withCommentOnlyChangedContentContext(
      final int commentOnlyChangedContentContext) {
    this.commentOnlyChangedContentContext = commentOnlyChangedContentContext;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withCommentTemplate(final String commentTemplate) {
    this.commentTemplate = commentTemplate;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withCreateCommentWithAllSingleFileComments(
      final boolean createCommentWithAllSingleFileComments) {
    this.createCommentWithAllSingleFileComments = createCommentWithAllSingleFileComments;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withMaxCommentSize(final Integer maxCommentSize) {
    this.maxCommentSize = maxCommentSize;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withMaxNumberOfViolations(
      final Integer maxNumberOfViolations) {
    this.maxNumberOfViolations = maxNumberOfViolations;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withWorkspace(final String workspace) {
    this.workspace = workspace;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withRepositorySlug(final String repositorySlug) {
    this.repositorySlug = repositorySlug;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withPullRequestId(final String pullRequestId) {
    this.pullRequestId = pullRequestId;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withShouldCreateSingleFileComment(
      final boolean shouldCreateSingleFileComment) {
    this.shouldCreateSingleFileComment = shouldCreateSingleFileComment;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withShouldKeepOldComments(
      final boolean shouldKeepOldComments) {
    this.shouldKeepOldComments = shouldKeepOldComments;
    return this;
  }

  public void toPullRequest() {
    final CommentsProvider commentsProvider = new BitbucketCloudCommentsProvider(this);
    createComments(violationsLogger, violations, commentsProvider);
  }
}
