package main

import java.sql.Timestamp

import slick.lifted.MappedTo

case class Topic(
                  topic: TPValues.Topic,
                  created_by: TPValues.Username,
                  created: Timestamp,
                  last_response: Timestamp,
                  id: TPValues.Id = TPValues.Id(0L)
                )

case class Post(
                 content: TPValues.Content,
                 username: TPValues.Username,
                 email: TPValues.Email,
                 created: Timestamp,
                 secret: TPValues.Secret,
                 topic_id: TPValues.Id = TPValues.Id(0L),
                 id: TPValues.Id = TPValues.Id(0L),
               )

case class CreateTopic(
                                  topic: TPValues.Topic,
                                  content: TPValues.Content,
                                  username: TPValues.Username,
                                  email: TPValues.Email
                                )

case class CreatePost(content: TPValues.Content, username: TPValues.Username, email: TPValues.Email)

case class UpdatePost(content: TPValues.Content)

case class TopicPosts(topic: Topic, posts: Seq[Post])

case class TopicPost(topic: Topic, post: Post)

object TPValues {

  case class Topic(value: String) extends AnyVal with MappedTo[String]

  case class Username(value: String) extends AnyVal with MappedTo[String]

  case class Id(value: Long) extends AnyVal with MappedTo[Long]

  case class Content(value: String) extends AnyVal with MappedTo[String]

  case class Email(value: String) extends AnyVal with MappedTo[String]

  case class Secret(value: String) extends AnyVal with MappedTo[String]



}

object TPValuesImplicits {
  import main.TPValues._

  implicit class StringToSecret(s : String) {
    def toSecret = Secret(s)
  }
  implicit class LongToId(l: Long) {
    def toId = Id(l)
  }

  implicit class StringToEmail(s:String) {
    def toEmail = Email(s)
  }

  implicit class StringToContent(s:String) {
    def toContent = Content(s)
  }

  implicit class StringToTopic(s:String) {
    def toTopic = TPValues.Topic(s)
  }

  implicit class StringToUsername(s:String) {
    def toUsername = Username(s)
  }
}
