package se.bjurr.violations.comments.bitbucketcloud.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.bjurr.bitbucketcloud.gen.api.RepositoriesApi;
import se.bjurr.bitbucketcloud.gen.model.CommentContent;
import se.bjurr.bitbucketcloud.gen.model.CommentInline;
import se.bjurr.bitbucketcloud.gen.model.Diffstat;
import se.bjurr.bitbucketcloud.gen.model.PaginatedActivities;
import se.bjurr.bitbucketcloud.gen.model.PaginatedDiffstats;
import se.bjurr.bitbucketcloud.gen.model.PaginatedPullrequestComments;
import se.bjurr.bitbucketcloud.gen.model.Pullrequest;
import se.bjurr.violations.comments.bitbucketcloud.lib.client.RestEasyClientFactory;
import se.bjurr.violations.comments.lib.CommentsProvider;
import se.bjurr.violations.comments.lib.model.ChangedFile;
import se.bjurr.violations.comments.lib.model.Comment;

public class BitbucketCloudCommentsProvider implements CommentsProvider {
  private static final Function<
          ? super se.bjurr.bitbucketcloud.gen.model.Comment, ? extends Comment>
      COMMENT_TO_COMMENT =
          (it) -> {
            final String identifier = it.getId() + "";
            final String content = it.getContent().getRaw();
            final String type = null;
            final List<String> specifics = new ArrayList<>();
            return new Comment(identifier, content, type, specifics);
          };

  private final ViolationCommentsToBitbucketCloudApi api;
  private final RepositoriesApi repositoryClient;
  private List<Diffstat> diffStat;

  public BitbucketCloudCommentsProvider(final ViolationCommentsToBitbucketCloudApi api) {
    this.api = api;
    this.repositoryClient = RestEasyClientFactory.create(RepositoriesApi.class, api);
  }

  @Override
  public void createCommentWithAllSingleFileComments(final String commentString) {
    final CommentContent content = new CommentContent();
    content.setRaw(commentString);

    final se.bjurr.bitbucketcloud.gen.model.Comment comment =
        new se.bjurr.bitbucketcloud.gen.model.Comment();
    comment.setContent(content);

    repositoryClient.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommentsPost(
        api.getWorkspace(), api.getRepositorySlug(), api.getPullRequestId(), comment);
  }

  @Override
  public void createSingleFileComment(
      final ChangedFile file, final Integer line, final String commentString) {
    final CommentContent content = new CommentContent();
    content.setRaw(commentString);

    final CommentInline inline = new CommentInline();
    inline.setPath(file.getFilename());
    inline.setTo(line);

    final se.bjurr.bitbucketcloud.gen.model.Comment comment =
        new se.bjurr.bitbucketcloud.gen.model.Comment();
    comment.setContent(content);
    comment.setInline(inline);

    repositoryClient.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommentsPost(
        api.getWorkspace(), api.getRepositorySlug(), api.getPullRequestId(), comment);
  }

  @Override
  public List<Comment> getComments() {
    final PaginatedActivities activities =
        repositoryClient.repositoriesUsernameRepoSlugPullrequestsPullRequestIdActivityGet(
            api.getWorkspace(), api.getRepositorySlug(), api.getPullRequestId());

    final List<Comment> comments =
        activities
            .getValues()
            .stream()
            .map(it -> it.getComment())
            .map(COMMENT_TO_COMMENT)
            .collect(Collectors.toList());

    final PaginatedPullrequestComments prComments =
        repositoryClient.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommentsGet(
            api.getWorkspace(), api.getRepositorySlug(), api.getPullRequestId());
    comments.addAll(
        prComments.getValues().stream().map(COMMENT_TO_COMMENT).collect(Collectors.toList()));

    return comments;
  }

  @Override
  public List<ChangedFile> getFiles() {
    final List<Diffstat> values = getDiffstat();
    return values
        .stream()
        .filter((it) -> isNotDeleted(it))
        .map(
            (it) -> {
              final String filename = it.getNew().getPath();
              final List<String> specifics = new ArrayList<>();
              return new ChangedFile(filename, specifics);
            })
        .collect(Collectors.toList());
  }

  private synchronized List<Diffstat> getDiffstat() {
    if (this.diffStat != null) {
      return this.diffStat;
    }
    final Pullrequest pr =
        repositoryClient.repositoriesUsernameRepoSlugPullrequestsPullRequestIdGet(
            api.getWorkspace(), api.getRepositorySlug(), api.getPullRequestId());
    final String spec =
        pr.getSource().getCommit().getHash() + ".." + pr.getDestination().getCommit().getHash();
    final Boolean ignoreWhitespace = true;
    final PaginatedDiffstats diff =
        repositoryClient.repositoriesUsernameRepoSlugDiffstatSpecGet(
            api.getWorkspace(), api.getRepositorySlug(), spec, ignoreWhitespace);
    this.diffStat = diff.getValues();
    return this.diffStat;
  }

  @Override
  public void removeComments(final List<Comment> comments) {
    final String username = api.getUsername();
    final String pullRequestId = api.getPullRequestId();
    final String repoSlug = api.getRepositorySlug();
    for (final Comment comment : comments) {
      final String commentId = comment.getIdentifier();
      repositoryClient.repositoriesUsernameRepoSlugPullrequestsPullRequestIdCommentsCommentIdDelete(
          username, pullRequestId, commentId, repoSlug);
    }
  }

  @Override
  public boolean shouldComment(final ChangedFile changedFile, final Integer line) {
    for (final Diffstat diffStat : getDiffstat()) {
      if (isNotDeleted(diffStat)) {
        if (isChanged(changedFile, diffStat)) {
          if (isChangeInsideContext(line, diffStat)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean shouldCreateCommentWithAllSingleFileComments() {
    return api.shouldCreateCommentWithAllSingleFileComments();
  }

  @Override
  public boolean shouldCreateSingleFileComment() {
    return api.shouldCreateSingleFileComment();
  }

  @Override
  public boolean shouldKeepOldComments() {
    return api.shouldKeepOldComments();
  }

  @Override
  public Optional<String> findCommentTemplate() {
    return api.findCommentTemplate();
  }

  @Override
  public Integer getMaxNumberOfViolations() {
    return api.getMaxNumberOfViolations();
  }

  @Override
  public Integer getMaxCommentSize() {
    return api.getMaxCommentSize();
  }

  private boolean isNotDeleted(final Diffstat diffStat) {
    return diffStat.getNew() != null;
  }

  private boolean isChangeInsideContext(final Integer line, final Diffstat diffStat) {
    return Math.abs(diffStat.getLinesAdded() - line) > api.getCommentOnlyChangedContentContext();
  }

  private boolean isChanged(final ChangedFile changedFile, final Diffstat diffStat) {
    return diffStat.getNew().getPath().endsWith(changedFile.getFilename())
        || changedFile.getFilename().endsWith(diffStat.getNew().getPath());
  }
}
