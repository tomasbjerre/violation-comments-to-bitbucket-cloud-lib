package se.bjurr.violations.comments.bitbucketcloud.lib;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.bjurr.bitbucketcloud.gen.api.RepositoriesApi;
import se.bjurr.bitbucketcloud.gen.model.*;
import se.bjurr.violations.comments.bitbucketcloud.lib.client.RestEasyClientFactory;
import se.bjurr.violations.comments.lib.CommentsProvider;
import se.bjurr.violations.comments.lib.model.ChangedFile;
import se.bjurr.violations.comments.lib.model.Comment;
import se.bjurr.violations.lib.util.PatchParserUtil;

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

  private String diffSpec;

  private final Map<String, String> diffsPerFile = new HashMap<>();

  public BitbucketCloudCommentsProvider(final ViolationCommentsToBitbucketCloudApi api) {
    this.api = api;
    this.repositoryClient = RestEasyClientFactory.create(RepositoriesApi.class, api);
  }

  @Override
  public void createComment(final String commentString) {
    final CommentContent content = new CommentContent();
    content.setRaw(commentString);

    final se.bjurr.bitbucketcloud.gen.model.Comment comment =
        new se.bjurr.bitbucketcloud.gen.model.Comment();
    comment.setContent(content);

    repositoryClient.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsPost(
        Integer.valueOf(api.getPullRequestId()),
        api.getRepositorySlug(),
        api.getWorkspace(),
        comment);
  }

  @Override
  public void createSingleFileComment(
      final ChangedFile file, final Integer lineInFile, final String commentString) {

    final CommentContent content = new CommentContent();
    content.setRaw(commentString);

    final CommentInline inline = new CommentInline();
    inline.setPath(file.getFilename());

    final Integer lineToComment = Optional.ofNullable(lineInFile).orElse(1);
    inline.setTo(lineToComment);

    final se.bjurr.bitbucketcloud.gen.model.Comment comment =
        new se.bjurr.bitbucketcloud.gen.model.Comment();
    comment.setContent(content);
    comment.setInline(inline);

    repositoryClient.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsPost(
        Integer.valueOf(api.getPullRequestId()),
        api.getRepositorySlug(),
        api.getWorkspace(),
        comment);
  }

  @Override
  public List<Comment> getComments() {
    final PaginatedActivities activities =
        repositoryClient.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdActivityGet(
            Integer.valueOf(api.getPullRequestId()), api.getRepositorySlug(), api.getWorkspace());

    final List<Comment> comments =
        activities.getValues().stream()
            .map(it -> it.getComment())
            .filter(it -> it != null)
            .map(COMMENT_TO_COMMENT)
            .collect(Collectors.toList());

    final PaginatedPullrequestComments prComments =
        repositoryClient.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsGet(
            Integer.valueOf(api.getPullRequestId()), api.getRepositorySlug(), api.getWorkspace());
    comments.addAll(
        prComments.getValues().stream().map(COMMENT_TO_COMMENT).collect(Collectors.toList()));

    return comments;
  }

  @Override
  public List<ChangedFile> getFiles() {
    final List<Diffstat> values = getDiffstat();
    return values.stream()
        .filter((it) -> isNotDeleted(it))
        .map(
            (it) -> {
              final String filename = it.getNew().getPath();
              final List<String> specifics = new ArrayList<>();
              return new ChangedFile(filename, specifics);
            })
        .collect(Collectors.toList());
  }

  @Override
  public void removeComments(final List<Comment> comments) {
    for (final Comment comment : comments) {
      final Long commentId = Long.valueOf(comment.getIdentifier());
      // Resolve rather than delete: every comment this library creates is a top-level PR
      // comment, and Bitbucket Cloud lets any top-level comment's thread be resolved. This
      // collapses it in the PR UI instead of erasing it outright, matching the resolvable
      // comments behavior already implemented for GitLab and Bitbucket Server.
      repositoryClient
          .repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdCommentsCommentIdResolvePost(
              commentId,
              Integer.valueOf(api.getPullRequestId()),
              api.getRepositorySlug(),
              api.getWorkspace());
    }
  }

  @Override
  public boolean shouldComment(final ChangedFile changedFile, final Integer line) {
    for (final Diffstat diffStat : getDiffstat()) {
      if (isNotDeleted(diffStat)) {
        if (isChanged(changedFile, diffStat)) {
          if (api.shouldCommentOnlyChangedContent()) {
            final String patchString = getDiff(changedFile.getFilename());
            final boolean lineChanged = new PatchParserUtil(patchString).isLineInDiff(line);
            if (lineChanged) {
              return true;
            }
          } else {
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

  private boolean isChanged(final ChangedFile changedFile, final Diffstat diffStat) {
    return diffStat.getNew().getPath().endsWith(changedFile.getFilename())
        || changedFile.getFilename().endsWith(diffStat.getNew().getPath());
  }

  private synchronized String getDiff(final String path) {
    if (!diffsPerFile.containsKey(path)) {
      final String repoSlug = this.api.getRepositorySlug();
      final String workspace = api.getWorkspace();
      final String spec = getDiffSpec();
      final Integer context = null;
      final Boolean ignoreWhitespace = null;
      final Boolean binary = null;
      final Boolean renames = null;
      final Boolean merge = null;
      final Boolean topic = null;
      final String diffString =
          repositoryClient.repositoriesWorkspaceRepoSlugDiffSpecGet(
              repoSlug,
              spec,
              workspace,
              context,
              path,
              ignoreWhitespace,
              binary,
              renames,
              merge,
              topic);
      this.diffsPerFile.put(path, diffString);
    }
    return this.diffsPerFile.get(path);
  }

  private synchronized List<Diffstat> getDiffstat() {
    if (this.diffStat != null) {
      return this.diffStat;
    }
    final String spec = getDiffSpec();
    final Boolean ignoreWhitespace = null;
    final Boolean merge = null;
    final String path = null;
    final Boolean renames = null;
    final Boolean topic = null;
    final PaginatedDiffstats diff =
        repositoryClient.repositoriesWorkspaceRepoSlugDiffstatSpecGet(
            api.getRepositorySlug(),
            spec,
            api.getWorkspace(),
            ignoreWhitespace,
            merge,
            path,
            renames,
            topic);
    this.diffStat = List.copyOf(diff.getValues());
    return this.diffStat;
  }

  private synchronized String getDiffSpec() {
    if (this.diffSpec == null) {
      final Pullrequest pr =
          repositoryClient.repositoriesWorkspaceRepoSlugPullrequestsPullRequestIdGet(
              Integer.valueOf(api.getPullRequestId()), api.getRepositorySlug(), api.getWorkspace());
      diffSpec =
          pr.getSource().getCommit().getHash() + ".." + pr.getDestination().getCommit().getHash();
    }
    return diffSpec;
  }

  @Override
  public boolean shouldCommentOnlyChangedFiles() {
    return api.shouldCommentOnlyChangedFiles();
  }

  public static List<se.bjurr.bitbucketcloud.gen.model.Comment> getOrderedComments(
      Set<? extends se.bjurr.bitbucketcloud.gen.model.Comment> values) {
    return values.stream()
        .sorted((a, b) -> a.getCreatedOn().compareTo(b.getCreatedOn()))
        .collect(Collectors.toList());
  }

  public static List<Commit> getOrderedCommits(Set<Commit> values) {
    return values.stream()
        .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
        .collect(Collectors.toList());
  }
}
