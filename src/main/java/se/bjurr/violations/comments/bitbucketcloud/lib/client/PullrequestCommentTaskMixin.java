package se.bjurr.violations.comments.bitbucketcloud.lib.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import se.bjurr.bitbucketcloud.gen.model.Comment;

/**
 * Bitbucket Cloud's task response embeds the comment it's anchored to as a minimal reference -
 * {@code {"id": ..., "links": {...}}}, with no {@code "type"} field (confirmed against a real task
 * on https://bitbucket.org/tomasbjerre/violations-test/pull-requests/1). Two things stand in the
 * way of binding that reference at all, both handled here:
 *
 * <ol>
 *   <li>The generated {@code Comment} model inherits {@code @JsonTypeInfo} from {@code
 *       ModelObject}, which normally uses "type" to pick a subtype (e.g. {@code
 *       PullrequestComment}); with it entirely absent, and FAIL_ON_INVALID_SUBTYPE disabled, the
 *       whole nested object silently deserializes to {@code null} instead of falling back to {@code
 *       Comment} itself. {@link #getComment()} turns polymorphic resolution off for just this one
 *       property so it binds as a plain {@code Comment}.
 *   <li>{@code Comment}'s own {@code @JsonCreator} constructor separately requires "type" as
 *       non-null, so binding as plain {@code Comment} still fails without it. {@link CommentMixin}
 *       relaxes that one parameter to optional.
 * </ol>
 *
 * Only {@link Comment#getId()} is ever read from the result, so neither relaxation loses anything
 * this library needs.
 */
abstract class PullrequestCommentTaskMixin {
  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  abstract Comment getComment();

  abstract static class CommentMixin {
    @JsonCreator
    CommentMixin(@JsonProperty(value = "type", required = false) final String type) {}
  }
}
