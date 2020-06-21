package validation

import appConfig.Config
import main.{CreatePost, CreateTopic, TPValues, UpdatePost}

trait Validator[T] {
  def validate(t: T): Option[ApiError]

  def getFirstDefined[A](s: Seq[Option[A]]): Option[A] = s match {
    case Nil =>
      None
    case option :: _ if option.isDefined =>
      option
    case _ =>
      getFirstDefined(s.tail)
  }

}

object ValidatorConsts extends Config {
  val maxContentLength = config.getInt("app.maxContentLength")
  val maxUsernameLength = config.getInt("app.maxUsernameLength")
  val maxTopicLength = config.getInt("app.maxTopicLength")

  def validateEmail(email: TPValues.Email) = {
    if (email.value.isEmpty)
      Some(ApiError.emptyEmailField)
    else if (!email.value.contains("@"))
      Some(ApiError.wrongEmailFormat)

    else None
  }

  def validateContent(content: TPValues.Content) = {
    if (content.value.isEmpty)
      Some(ApiError.emptyContentField)
    else if (content.value.length > maxContentLength)
      Some(ApiError.contentTooLong)
    else
      None
  }

  def validateUsername(username: TPValues.Username) = {
    if (username.value.isEmpty)
      Some(ApiError.emptyUsernameField)
    else if (username.value.length > maxUsernameLength)
      Some(ApiError.usernameTooLong)
    else
      None
  }

  def validateTopic(topic: TPValues.Topic) = {
    if (topic.value.isEmpty)
      Some(ApiError.emptyTopicField)
    else if (topic.value.length > maxTopicLength)
      Some(ApiError.topicTooLong)
    else
      None
  }
}

object CreateTopicValidator extends Validator[CreateTopic] {

  import ValidatorConsts._

  def validate(createDiscussionTopic: CreateTopic): Option[ApiError] = {
    val validatedUsername = validateUsername(createDiscussionTopic.username)
    val validatedTopic = validateTopic(createDiscussionTopic.topic)
    val validatedContent = validateContent(createDiscussionTopic.content)
    val validatedEmail = validateEmail(createDiscussionTopic.email)

    getFirstDefined(
      Seq(
        validatedUsername,
        validatedTopic,
        validatedContent,
        validatedEmail
      )
    )
  }
}

object UpdatePostValidator extends Validator[UpdatePost] {

  import ValidatorConsts._

  def validate(updatePost: UpdatePost): Option[ApiError] = validateContent(updatePost.content)
}

object CreatePostValidator extends Validator[CreatePost] {

  import ValidatorConsts._

  def validate(createPost: CreatePost): Option[ApiError] = {
    val validatedUsername = validateUsername(createPost.username)
    val validatedContent = validateContent(createPost.content)
    val validatedEmail = validateEmail(createPost.email)

    getFirstDefined(
      Seq(
        validatedUsername,
        validatedContent,
        validatedEmail
      )
    )
  }
}

