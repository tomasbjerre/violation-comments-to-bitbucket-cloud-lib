package se.bjurr.violations.comments.bitbucketcloud.lib;

import static se.bjurr.violations.comments.lib.CommentsCreator.createComments;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.LoggerFactory;
import se.bjurr.violations.comments.lib.CommentsProvider;
import se.bjurr.violations.lib.ViolationsLogger;
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

  /** https://support.atlassian.com/bitbucket-cloud/docs/api-tokens/ */
  private String apiToken;

  private String workspace;
  private String repositorySlug;
  private String pullRequestId;
  private boolean createCommentWithAllSingleFileComments;
  private boolean shouldCreateSingleFileComment;
  private boolean shouldKeepOldComments;
  private String commentTemplate;
  private Integer maxCommentSize;
  private Integer maxNumberOfViolations;
  private Set<Violation> violations;

  private boolean shouldCommentOnlyChangedContent;

  private boolean shouldCommentOnlyChangedFiles = true;

  public ViolationCommentsToBitbucketCloudApi() {}

  public static ViolationCommentsToBitbucketCloudApi violationCommentsToBitbucketCloudApi() {
    return new ViolationCommentsToBitbucketCloudApi();
  }

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

  public String getApiToken() {
    return this.apiToken;
  }

  public String getWorkspace() {
    return this.workspace;
  }

  public String getRepositorySlug() {
    return this.repositorySlug;
  }

  public String getPullRequestId() {
    return this.pullRequestId;
  }

  public boolean shouldCreateCommentWithAllSingleFileComments() {
    return this.createCommentWithAllSingleFileComments;
  }

  public boolean shouldCreateSingleFileComment() {
    return this.shouldCreateSingleFileComment;
  }

  public boolean shouldKeepOldComments() {
    return this.shouldKeepOldComments;
  }

  public Optional<String> findCommentTemplate() {
    return Optional.ofNullable(this.commentTemplate);
  }

  public Integer getMaxCommentSize() {
    return this.maxCommentSize;
  }

  public Integer getMaxNumberOfViolations() {
    return this.maxNumberOfViolations;
  }

  public ViolationsLogger getViolationsLogger() {
    return this.violationsLogger;
  }

  public boolean shouldCommentOnlyChangedContent() {
    return this.shouldCommentOnlyChangedContent;
  }

  public ViolationCommentsToBitbucketCloudApi withPassword(final String password) {
    this.password = password;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withUsername(final String username) {
    this.username = username;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withApiToken(final String apiToken) {
    this.apiToken = apiToken;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withViolationsLogger(
      final ViolationsLogger violationsLogger) {
    this.violationsLogger = violationsLogger;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withViolations(final Set<Violation> violations) {
    this.violations = violations;
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

  public ViolationCommentsToBitbucketCloudApi withCreateSingleFileComment(
      final boolean shouldCreateSingleFileComment) {
    this.shouldCreateSingleFileComment = shouldCreateSingleFileComment;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withKeepOldComments(
      final boolean shouldKeepOldComments) {
    this.shouldKeepOldComments = shouldKeepOldComments;
    return this;
  }

  public ViolationCommentsToBitbucketCloudApi withShouldCommentOnlyChangedContent(
      final boolean shouldCommentOnlyChangedContent) {
    this.shouldCommentOnlyChangedContent = shouldCommentOnlyChangedContent;
    return this;
  }

  public void toPullRequest() {
    final CommentsProvider commentsProvider = new BitbucketCloudCommentsProvider(this);
    createComments(this.violationsLogger, this.violations, commentsProvider);
  }

  public boolean shouldCommentOnlyChangedFiles() {
    return this.shouldCommentOnlyChangedFiles;
  }

  public ViolationCommentsToBitbucketCloudApi withShouldCommentOnlyChangedFiles(
      final boolean shouldCommentOnlyChangedFiles) {
    this.shouldCommentOnlyChangedFiles = shouldCommentOnlyChangedFiles;
    return this;
  }
}
