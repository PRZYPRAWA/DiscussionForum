package Validation

import AppConfig.Config
import Main.{CreateDiscussionTopic, CreatePost, UpdatePost}

trait Validator[T] {
  def validate(t: T): Option[ApiError]
}

object ValidatorConsts extends Config {
  val maxContentLength = config.getInt("app.maxContentLength")
  val maxUsernameLength = config.getInt("app.maxUsernameLength")
  val maxTopicLength = config.getInt("app.maxTopicLength")
  val emailRegex = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$"

  def validateEmail(email: String) = {
    if (!email.matches(emailRegex))
      Some(ApiError.wrongEmailFormat)
    else if (email.isEmpty)
      Some(ApiError.emptyEmailField)
    else None
  }

  def validateContent(content: String) = {
    if (content.isEmpty)
      Some(ApiError.emptyContentField)
    else if (content.length > maxContentLength)
      Some(ApiError.contentTooLong)
    else
      None
  }

  def validateUsername(username: String) = {
    if (username.isEmpty)
      Some(ApiError.emptyUsernameField)
    else if (username.length > maxUsernameLength)
      Some(ApiError.usernameTooLong)
    else
      None
  }

  def validateTopic(topic: String) = {
    if (topic.isEmpty)
      Some(ApiError.emptyTopicField)
    else if (topic.length > maxTopicLength)
      Some(ApiError.topicTooLong)
    else
      None
  }
}

object CreateTopicValidator extends Validator[CreateDiscussionTopic] {

  import ValidatorConsts._

  def validate(createDiscussionTopic: CreateDiscussionTopic): Option[ApiError] = {
    val validatedUsername = validateUsername(createDiscussionTopic.nick)
    val validatedTopic = validateTopic(createDiscussionTopic.topic)
    val validatedContent = validateContent(createDiscussionTopic.content)
    val validatedEmail = validateEmail(createDiscussionTopic.email)

    if (validatedUsername.isDefined)
      validatedUsername
    else if (validatedTopic.isDefined)
      validatedTopic
    else if (validatedContent.isDefined)
      validatedContent
    else if (validatedEmail.isDefined)
      validatedEmail
    else
      None
  }
}

object UpdatePostValidator extends Validator[UpdatePost] {

  import ValidatorConsts._

  def validate(updatePost: UpdatePost): Option[ApiError] = validateContent(updatePost.content)
}

object CreatePostValidator extends Validator[CreatePost] {

  import ValidatorConsts._

  def validate(createPost: CreatePost): Option[ApiError] = {
    val validatedUsername = validateUsername(createPost.nick)
    val validatedContent = validateContent(createPost.content)
    val validatedEmail = validateEmail(createPost.email)

    if (validatedUsername.isDefined)
      validatedUsername
    else if (validatedContent.isDefined)
      validatedContent
    else if (validatedEmail.isDefined)
      validatedEmail
    else
      None
  }
}