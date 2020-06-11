package Main

import AppConfig.Config
import Database.{NoPost, NoTopic, Repository}
import Validation._
import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContextExecutor

trait Service extends Protocols with TodoDirectives with ValidatorDirectives {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  val logger: LoggingAdapter
  val database = Repository

  val topicRouter = pathPrefix("topics") {
    (get & path(LongNumber)) { id =>
      parameters('before.as[Int].?, 'after.as[Int].?) { (before: Option[Int], after: Option[Int]) =>
        parameter("offset".as[Int].?) { offset: Option[Int] =>
          handleWithGeneric(database.topicWithPosts(id, offset, before, after)) { posts =>
            complete(posts)
          }
        }
      }
    } ~
      (post & path(LongNumber)) { id =>
        entity(as[CreatePost]) { createPost =>
          validateWith(CreatePostValidator)(createPost) {
            handle(database.addPost(id, createPost)) {
              case NoTopic(_) => ApiError.topicNotFound(id.toString)
              case _ => ApiError.generic
            } { posts =>
              complete(posts)
            }
          }
        }
      } ~
      (post & entity(as[CreateDiscussionTopic])) { createDiscussionTopic =>
        validateWith(CreateTopicValidator)(createDiscussionTopic) {
          handleWithGeneric(database.addTopic(createDiscussionTopic)) { topic =>
            complete(topic)
          }
        }
      } ~
      (get & parameters('limit.as[Int].?, 'offset.as[Int].?)) { (limit: Option[Int], offset: Option[Int]) =>
        handleWithGeneric(database.allTopics(limit, offset)) { topics =>
          complete(topics)
        }
      }
  }

  val postRouter = pathPrefix("posts") {
    (get & parameters('limit.as[Int].?, 'offset.as[Int].?)) { (limit: Option[Int], offset: Option[Int]) =>
      handleWithGeneric(database.allPosts(limit, offset)) { posts =>
        complete(posts)
      }
    } ~
      (put & path(Segment)) { secret =>
        entity(as[UpdatePost]) { updatePost =>
          validateWith(UpdatePostValidator)(updatePost) {
            handle(database.updatePost(secret, updatePost)) {
              case NoPost(_) => ApiError.postNotFound(secret)
              case _ => ApiError.generic
            } { post =>
              complete(post)
            }
          }
        }
      } ~
      (delete & path(Segment)) { secret =>
        handle(database.deletePost(secret)) {
          case NoPost(_) => ApiError.postNotFound(secret)
          case _ => ApiError.generic
        } { rowsAffected =>
          complete(rowsAffected.toString)
        }
      }
  }

  val routes = {
    logRequestResult("akka-http-forum") {
      topicRouter ~ postRouter
    }
  }


}

object ForumMain extends App with Service with Config {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
