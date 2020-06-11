package Validation

import Main.{CreateDiscussionTopic, CreatePost, UpdatePost}

trait Validator[T] {
  def validate(t: T): Option[ApiError]
}

object CreateTopicValidator extends Validator[CreateDiscussionTopic] {

  def validate(createDiscussionTopic: CreateDiscussionTopic): Option[ApiError] =
    if (createDiscussionTopic.content.isEmpty)
      Some(ApiError.emptyContentField)
    else if (createDiscussionTopic.email.isEmpty)
      Some(ApiError.emptyEmailField)
    else if (createDiscussionTopic.nick.isEmpty)
      Some(ApiError.emptyUsernameField)
    else if (createDiscussionTopic.topic.isEmpty)
      Some(ApiError.emptyTopicField)
    else
      None
}

object UpdatePostValidator extends Validator[UpdatePost] {

  def validate(updatePost: UpdatePost): Option[ApiError] =
    if (updatePost.content.isEmpty)
      Some(ApiError.emptyContentField)
    else
      None
}

object CreatePostValidator extends Validator[CreatePost] {

  def validate(createPost: CreatePost): Option[ApiError] =
    if (createPost.content.isEmpty)
      Some(ApiError.emptyContentField)
    else if (createPost.email.isEmpty)
      Some(ApiError.emptyEmailField)
    else if (createPost.nick.isEmpty)
      Some(ApiError.emptyUsernameField)
    else
      None
}