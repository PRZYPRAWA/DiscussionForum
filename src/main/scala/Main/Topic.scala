package Main

import java.sql.Timestamp

import Validation.ApiError
import akka.http.scaladsl.model.StatusCode
import spray.json.{DefaultJsonProtocol, RootJsonFormat, _}

case class Topic(
                  topic: String,
                  created_by: String,
                  created: Timestamp,
                  last_response: Timestamp,
                  id: Long = 0L
                )

case class Post(
                 content: String,
                 username: String,
                 email: String,
                 created: Timestamp,
                 secret: String,
                 topic_id: Long = 0L,
                 id: Long = 0L
               )

case class CreateDiscussionTopic(topic: String, content: String, nick: String, email: String)

case class CreatePost(content: String, nick: String, email: String)

case class UpdatePost(content: String)

case class TopicPosts(topic: Topic, posts: Seq[Post])

case class TopicPost(topic: Topic, post: Post)


trait Protocols extends DefaultJsonProtocol {

  implicit object TimestampJsonFormat extends RootJsonFormat[Timestamp] {
    def write(t: Timestamp) = JsString(t.toString)

    def read(value: JsValue) = value match {
      case JsString(value) =>
        Timestamp.valueOf(value)
      case _ => deserializationError("Timestamp expected")
    }
  }

  implicit object StatusCodeFormat extends RootJsonFormat[StatusCode] {
    def write(s: StatusCode) = JsNumber(s.intValue())

    def read(value: JsValue) = value match {
      case JsNumber(value) =>
        StatusCode.int2StatusCode(value.intValue)
      case _ => deserializationError("StatusCode expected")
    }
  }

  implicit val topicFormat = jsonFormat5(Topic.apply)
  implicit val createDiscussionTopicFormat = jsonFormat4(CreateDiscussionTopic.apply)

  implicit val postFormat = jsonFormat7(Post.apply)
  implicit val createPostFormat = jsonFormat3(CreatePost.apply)
  implicit val updatePostFormat = jsonFormat1(UpdatePost.apply)

  implicit val topicPostsFormat = jsonFormat2(TopicPosts.apply)
  implicit val topicPostFormat = jsonFormat2(TopicPost.apply)

  implicit val apiErrorFormat = jsonFormat2(ApiError.apply)
}
