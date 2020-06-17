package main

import akka.http.scaladsl.server.Directives
import database.ForumRepository.{NoPost, NoTopic}
import database.Repository
import validation._
import TPValuesImplicits._

class ForumRouter(database: Repository) extends Directives with TopicDirectives with ValidatorDirectives {

  def route = logRequestResult("akka-http-forum") {
    topicRoute ~ postRoute
  }

  val topicRoute = pathPrefix("topics") {
    (get & path(LongNumber)) { id =>
      parameters(Symbol("before").as[Int].?, Symbol("after").as[Int].?) { (before: Option[Int], after: Option[Int]) =>
        parameter("offset".as[Int].?) { offset: Option[Int] =>
          handle(database.topicWithPosts(id.toId, offset, before, after)) {
            case NoTopic(_) => ApiError.topicNotFound(id.toString)
            case _ => ApiError.generic
          } { posts =>
            complete(posts)
          }
        }
      }
    } ~
      (post & path(LongNumber)) { id =>
        entity(as[CreatePost]) { createPost =>
          validateWith(CreatePostValidator)(createPost) {
            handle(database.addPost(id.toId, createPost)) {
              case NoTopic(_) => ApiError.topicNotFound(id.toString)
              case _ => ApiError.generic
            } { posts =>
              complete(posts)
            }
          }
        }
      } ~
      (post & entity(as[CreateTopic])) { createDiscussionTopic =>
        validateWith(CreateTopicValidator)(createDiscussionTopic) {
          handleWithGeneric(database.addTopic(createDiscussionTopic)) { topic =>
            complete(topic)
          }
        }
      } ~
      (get & parameters(Symbol("limit").as[Int].?, Symbol("offset").as[Int].?)) { (limit: Option[Int], offset: Option[Int]) =>
        handleWithGeneric(database.getTopics(limit, offset)) { topics =>
          complete(topics)
        }
      }
  }

  val postRoute = pathPrefix("posts") {
    (get & parameters(Symbol("limit").as[Int].?, Symbol("offset").as[Int].?)) { (limit: Option[Int], offset: Option[Int]) =>
      handleWithGeneric(database.getPosts(limit, offset)) { posts =>
        complete(posts)
      }
    } ~
      (put & path(Segment)) { secret =>
        entity(as[UpdatePost]) { updatePost =>
          validateWith(UpdatePostValidator)(updatePost) {
            handle(database.updatePost(secret.toSecret, updatePost)) {
              case NoPost(_) => ApiError.postNotFound(secret)
              case _ => ApiError.generic
            } { post =>
              complete(post)
            }
          }
        }
      } ~
      (delete & path(Segment)) { secret =>
        handle(database.deletePost(secret.toSecret)) {
          case NoPost(_) => ApiError.postNotFound(secret)
          case _ => ApiError.generic
        } { rowsAffected =>
          complete(rowsAffected.toString)
        }
      }
  }
}
