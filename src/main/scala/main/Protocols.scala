package main

import java.sql.Timestamp

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCode
import spray.json.{DefaultJsonProtocol, JsNumber, JsString, JsValue, RootJsonFormat, deserializationError}
import validation.ApiError

trait ValuesProtocols extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object TopicValueJsonFormat extends RootJsonFormat[TPValues.Topic] {
    def write(t: TPValues.Topic) = JsString(t.value)

    def read(value: JsValue) = value match {
      case JsString(value) =>
        TPValues.Topic(value)
      case _ => deserializationError("Timestamp expected")
    }
  }

  implicit object UsernameValueJsonFormat extends RootJsonFormat[TPValues.Username] {
    def write(u: TPValues.Username) = JsString(u.value)

    def read(value: JsValue) = value match {
      case JsString(value) =>
        TPValues.Username(value)
      case _ => deserializationError("Timestamp expected")
    }
  }

  implicit object IdValueJsonFormat extends RootJsonFormat[TPValues.Id] {
    def write(i: TPValues.Id) = JsNumber(i.value)

    def read(value: JsValue) = value match {
      case JsNumber(value) =>
        TPValues.Id(value.toLongExact)
      case _ => deserializationError("Timestamp expected")
    }
  }

  implicit object ContentValueJsonFormat extends RootJsonFormat[TPValues.Content] {
    def write(c: TPValues.Content) = JsString(c.value)

    def read(value: JsValue) = value match {
      case JsString(value) =>
        TPValues.Content(value)
      case _ => deserializationError("Timestamp expected")
    }
  }

  implicit object EmailValueJsonFormat extends RootJsonFormat[TPValues.Email] {
    def write(e: TPValues.Email) = JsString(e.value)

    def read(value: JsValue) = value match {
      case JsString(value) =>
        TPValues.Email(value)
      case _ => deserializationError("Timestamp expected")
    }
  }

  implicit object SecretValueJsonFormat extends RootJsonFormat[TPValues.Secret] {
    def write(s: TPValues.Secret) = JsString(s.value)

    def read(value: JsValue) = value match {
      case JsString(value) =>
        TPValues.Secret(value)
      case _ => deserializationError("Timestamp expected")
    }
  }

}

trait Protocols extends DefaultJsonProtocol with SprayJsonSupport with ValuesProtocols {

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
  implicit val createDiscussionTopicFormat = jsonFormat4(CreateTopic.apply)

  implicit val postFormat = jsonFormat7(Post.apply)
  implicit val createPostFormat = jsonFormat3(CreatePost.apply)
  implicit val updatePostFormat = jsonFormat1(UpdatePost.apply)

  implicit val topicPostsFormat = jsonFormat2(TopicPosts.apply)
  implicit val topicPostFormat = jsonFormat2(TopicPost.apply)

  implicit val apiErrorFormat = jsonFormat2(ApiError.apply)
}