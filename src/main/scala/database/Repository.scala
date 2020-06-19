package database

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import appConfig.Config
import database.Queries.{NoPost, NoTopic}
import main._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.math.max


trait Repository {

  def getTopics(limit: Option[Int], offset: Option[Int]): Future[Seq[Topic]]

  def getPosts(limit: Option[Int], offset: Option[Int]): Future[Seq[Post]]

  def getPost(postId: TPValues.Id): Future[Option[Post]]

  def getTopic(topicId: TPValues.Id): Future[Option[Topic]]

  def topicWithPosts(topicId: TPValues.Id, offset: Option[Int], before: Option[Int], after: Option[Int]): Future[TopicPosts]

  def addTopic(createTopic: CreateTopic): Future[TopicPost]

  def addPost(topicId: TPValues.Id, createPost: CreatePost): Future[Post]

  def updatePost(postSecret: TPValues.Secret, updatePost: UpdatePost): Future[Post]

  def deletePost(postSecret: TPValues.Secret): Future[Int]
}

object Queries {

  case class NoPost(secret: TPValues.Secret) extends Exception("")

  case class NoTopic(id: TPValues.Id) extends Exception("")

}

class Queries(implicit val profile: DatabaseModule) extends Config { //Repository with Config {
  import profile.profile.api._

  val LIMIT = config.getInt("app.limit")
  val AFTER = LIMIT * 2 / 3
  val BEFORE = LIMIT / 3

  lazy val topics = TableQuery[TopicTable]
  lazy val posts = TableQuery[PostTable]

  def getTopics(limit: Option[Int] = None, offset: Option[Int] = None) = {
    val tmpLimit = limit.getOrElse(0)
    val lim = if (tmpLimit <= 0 || tmpLimit > LIMIT) LIMIT else tmpLimit


    val tmpOffset = offset.getOrElse(0)
    val off = max(0, tmpOffset)

    topics.sortBy(_.last_response.desc).drop(off).take(lim).result
  }


  def getPosts(limit: Option[Int] = None, offset: Option[Int] = None) = {
    val tmpLimit = limit.getOrElse(0)
    val lim = if (tmpLimit <= 0 || tmpLimit > LIMIT) LIMIT else tmpLimit

    val tmpOffset = offset.getOrElse(0)
    val off = max(0, tmpOffset)

    posts.drop(off).take(lim).result
  }

  private def countAfterBefore(aft: Int, bef: Int): (Int, Int) = {
    val sum = aft + bef + 1
    if (sum > LIMIT) (LIMIT * aft / sum, LIMIT * bef / sum)
    else if (aft == 0 && bef == 0) (AFTER, BEFORE)
    else (aft, bef)
  }

  def topicWithPosts(topicId: TPValues.Id,
                     offset: Option[Int] = None,
                     before: Option[Int] = None,
                     after: Option[Int] = None) = {
    val tmpBefore = before.getOrElse(0)
    val bef1 = if (tmpBefore < 0 || tmpBefore > BEFORE) BEFORE else tmpBefore


    val tmpAfter = after.getOrElse(0)
    val aft1 = if (tmpAfter < 0 || tmpAfter > AFTER) AFTER else tmpAfter

    val (aft2, bef2) = countAfterBefore(aft1, bef1)

    val tmpOff = offset.getOrElse(0)
    val off1 = max(0, tmpOff)
    val off2 = max(0, off1 - bef2)

    {
      for {
        topic <- topics.filter(_.id === topicId).result.headOption
        posts <- topic match {
          case Some(_) => posts
            .filter(_.topic_id === topicId)
            .sortBy(_.created)
            .drop(off2)
            .take(aft2 + bef2 + 1)
            .result
          case None => DBIO.failed(NoTopic(topicId))
        }
      } yield main.TopicPosts(topic.get, posts)
    }
  }


  def addTopic(createTopic: CreateTopic) = {
    val date = Timestamp.valueOf(LocalDateTime.now())
    val topic = Topic(
      createTopic.topic,
      createTopic.username,
      date,
      date
    )

    {
      for {
        newTopic <- topics returning topics += topic
        newPost <- {
          val post = Post(
            createTopic.content,
            createTopic.username,
            createTopic.email,
            date,
            TPValues.Secret(UUID.randomUUID().toString),
            newTopic.id
          )

          posts returning posts += post
        }
      } yield main.TopicPost(newTopic, newPost)
    }
  }

  def addPost(topicId: TPValues.Id, createPost: CreatePost) = {
    val date = Timestamp.valueOf(LocalDateTime.now())
    for {
      tid <- topics.filter(_.id === topicId).result.headOption
      newPost <- tid match {
        case Some(_) =>
          val post = Post(
            createPost.content,
            createPost.username,
            createPost.email,
            date,
            TPValues.Secret(UUID.randomUUID().toString),
            topicId
          )

          topics.filter(_.id === topicId).map(_.last_response).update(date) >>
            (posts returning posts += post)
        case None => DBIO.failed(NoTopic(topicId))
      }
    } yield newPost
  }

  def updatePost(postSecret: TPValues.Secret, updatePost: UpdatePost) = {
    for {
      exists <- posts.filter(_.secret === postSecret).result.headOption
      post <- exists match {
        case Some(_) => posts.filter(_.secret === postSecret).map(_.content).update(updatePost.content) andThen
          posts.filter(_.secret === postSecret).result.head
        case None => DBIO.failed(NoPost(postSecret))
      }
    } yield post
  }

  def deletePost(postSecret: TPValues.Secret) = {
    posts.filter(_.secret === postSecret).delete
  }

  def getPost(postId: TPValues.Id) = {
    posts.filter(_.id === postId).result.headOption
  }

  def getTopic(topicId: TPValues.Id) = {
    topics.filter(_.id === topicId).result.headOption
  }
}
