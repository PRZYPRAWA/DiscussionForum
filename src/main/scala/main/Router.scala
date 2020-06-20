package main

import akka.http.scaladsl.server.Directives
import database.Queries.{NoPost, NoTopic}
import database.ForumRepository
import main.TPValuesImplicits._
import validation._

class ForumRouter(val repo: ForumRepository) extends Directives with TopicDirectives with ValidatorDirectives {

  def route = logRequestResult("akka-http-forum") {
    topicRoute ~ postRoute
  }

  val topicRoute = pathPrefix("topics") {
    (get & path(LongNumber)) { id =>
      parameters(Symbol("before").as[Int].?, Symbol("after").as[Int].?) { (before: Option[Int], after: Option[Int]) =>
        parameter("offset".as[Int].?) { offset: Option[Int] =>
          handle(repo.topicWithPosts(id.toId, offset, before, after)) {
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
            handle(repo.addPost(id.toId, createPost)) {
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
          handleWithGeneric(repo.addTopic(createDiscussionTopic)) { topic =>
            complete(topic)
          }
        }
      } ~
      (get & parameters(Symbol("limit").as[Int].?, Symbol("offset").as[Int].?)) { (limit: Option[Int], offset: Option[Int]) =>
        handleWithGeneric(repo.getTopics(limit, offset)) { topics =>
          complete(topics)
        }
      }
  }

  val postRoute = pathPrefix("posts") {
    (get & parameters(Symbol("limit").as[Int].?, Symbol("offset").as[Int].?)) { (limit: Option[Int], offset: Option[Int]) =>
      handleWithGeneric(repo.getPosts(limit, offset)) { posts =>
        complete(posts)
      }
    } ~
      (put & path(Segment)) { secret =>
        entity(as[UpdatePost]) { updatePost =>
          validateWith(UpdatePostValidator)(updatePost) {
            handle(repo.updatePost(secret.toSecret, updatePost)) {
              case NoPost(_) => ApiError.postNotFound(secret)
              case _ => ApiError.generic
            } { post =>
              complete(post)
            }
          }
        }
      } ~
      (delete & path(Segment)) { secret =>
        handle(repo.deletePost(secret.toSecret)) {
          case NoPost(_) => ApiError.postNotFound(secret)
          case _ => ApiError.generic
        } { rowsAffected =>
          complete(rowsAffected)
        }
      }
  }
}
