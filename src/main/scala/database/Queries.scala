package database

import appConfig.Config
import database.Queries.{NoPost, NoTopic}
import main._

import scala.concurrent.ExecutionContext.Implicits.global

object Queries {

  case class NoPost(secret: TPValues.Secret) extends Exception("")

  case class NoTopic(id: TPValues.Id) extends Exception("")

}

class Queries(implicit val profile: DatabaseModule) extends Config {

  import profile.profile.api._

  lazy val topics = TableQuery[TopicTable]
  lazy val posts = TableQuery[PostTable]

  def getTopics(limit: Int, offset: Int) =
    topics.sortBy(_.last_response.desc).drop(offset).take(limit).result

  def getPosts(limit: Int, offset: Int) =
    posts.drop(offset).take(limit).result

  def topicWithPosts(topicId: TPValues.Id, offset: Int, before: Int, after: Int) =
    for {
      topic <- topics.filter(_.id === topicId).result.headOption
      posts <- topic match {
        case Some(_) => posts
          .filter(_.topic_id === topicId)
          .sortBy(_.created)
          .drop(offset)
          .take(after + before + 1)
          .result
        case None => DBIO.failed(NoTopic(topicId))
      }
    } yield main.TopicPosts(topic.get, posts)


  def addTopic(topic: Topic, post: Post) = for {
    newTopic <- topics returning topics += topic
    newPost <- posts returning posts += post.copy(topic_id = newTopic.id)
  } yield main.TopicPost(newTopic, newPost)


  def addPost(topicId: TPValues.Id, post: Post) =
    for {
      tid <- topics.filter(_.id === topicId).result.headOption
      newPost <- tid match {
        case Some(_) =>
          topics.filter(_.id === topicId).map(_.last_response).update(post.created) andThen
            (posts returning posts += post.copy(topic_id = topicId))
        case None => DBIO.failed(NoTopic(topicId))
      }
    } yield newPost


  def updatePost(postSecret: TPValues.Secret, updatePost: UpdatePost) =
    for {
      exists <- posts.filter(_.secret === postSecret).result.headOption
      post <- exists match {
        case Some(_) => posts.filter(_.secret === postSecret).map(_.content).update(updatePost.content) andThen
          posts.filter(_.secret === postSecret).result.head
        case None => DBIO.failed(NoPost(postSecret))
      }
    } yield post


  def deletePost(postSecret: TPValues.Secret) =
    for {
      d <- posts.filter(_.secret === postSecret).delete
      isDeleted <- d match {
        case 0 => DBIO.failed(NoPost(postSecret))
        case 1 => DBIO.successful(d)
        case _ => DBIO.failed(new Exception(""))
      }
    } yield Deleted(isDeleted)

  def getPost(postId: TPValues.Id) = {
    posts.filter(_.id === postId).result.headOption
  }

  def getTopic(topicId: TPValues.Id) = {
    topics.filter(_.id === topicId).result.headOption
  }
}
