package Database

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import AppConfig.Config
import Main._
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


trait ForumRepository {

  def getTopics(limit: Option[Int], offset: Option[Int]): Future[Seq[Topic]]

  def getPosts(limit: Option[Int], offset: Option[Int]): Future[Seq[Post]]

  def getPost(postId: Long): Future[Option[Post]]

  def getTopic(topicId: Long): Future[Option[Topic]]

  def topicWithPosts(topicId: Long, offset: Option[Int], before: Option[Int], after: Option[Int]): Future[TopicPosts]

  def addTopic(createTopic: CreateDiscussionTopic): Future[TopicPost]

  def addPost(topicId: Long, createPost: CreatePost): Future[Post]

  def updatePost(postSecret: String, updatePost: UpdatePost): Future[Post]

  def deletePost(postSecret: String): Future[Int]

}

case class NoPost(msg: String) extends Exception(msg)

case class NoTopic(msg: String) extends Exception(msg)

object Repository extends Connection with ForumRepository with Config {
  val LIMIT = config.getInt("app.limit")
  val AFTER = LIMIT * 2 / 3
  val BEFORE = LIMIT / 3

  lazy val topics = TableQuery[TopicTable]
  lazy val posts = TableQuery[PostTable]

  override def getTopics(limit: Option[Int] = None, offset: Option[Int] = None): Future[Seq[Topic]] = {
    val tmpLimit = limit.getOrElse(0)
    val lim = if (tmpLimit <= 0 || tmpLimit > LIMIT) LIMIT else tmpLimit

    val tmpOffset = offset.getOrElse(0)
    val off = if (tmpOffset < 0) 0 else tmpOffset

    db.run(topics.sortBy(_.last_response.desc).drop(off).take(lim).result)
  }


  override def getPosts(limit: Option[Int] = None, offset: Option[Int] = None): Future[Seq[Post]] = {
    val tmpLimit = limit.getOrElse(0)
    val lim = if (tmpLimit <= 0 || tmpLimit > LIMIT) LIMIT else tmpLimit

    val tmpOffset = offset.getOrElse(0)
    val off = if (tmpOffset < 0) 0 else tmpOffset

    db.run(posts.drop(off).take(lim).result)
  }

  override def topicWithPosts(topicId: Long,
                              offset: Option[Int] = None,
                              before: Option[Int] = None,
                              after: Option[Int] = None): Future[TopicPosts] = {
    val tmpBefore = before.getOrElse(0)
    val bef1 = if (tmpBefore < 0 || tmpBefore > BEFORE) BEFORE else tmpBefore

    val tmpAfter = after.getOrElse(0)
    val aft1 = if (tmpAfter < 0 || tmpAfter > AFTER) AFTER else tmpAfter

    val sum = aft1 + bef1 + 1
    val (aft2, bef2) =
      if (sum > LIMIT) (LIMIT * aft1 / sum, LIMIT * bef1 / sum)
      else if (aft1 == 0 && bef1 == 0) (AFTER, BEFORE)
      else (aft1, bef1)

    val tmpOff = offset.getOrElse(0)
    val off1 = if (tmpOff < 0) 0 else tmpOff
    val off2 = if (off1 - bef2 < 0) 0 else off1 - bef2

    db.run {
      for {
        topic <- {
          topics.filter(_.id === topicId).result.headOption
        }
        posts <- topic match {
          case Some(_) => posts
            .filter(_.topic_id === topicId)
            .sortBy(_.created)
            .drop(off2)
            .take(aft2 + bef2 + 1)
            .result
          case None => DBIO.failed(NoTopic("topicWithPosts"))
        }
      } yield Main.TopicPosts(topic.get, posts)
    }
  }


  override def addTopic(createTopic: CreateDiscussionTopic): Future[TopicPost] = {
    val date = Timestamp.valueOf(LocalDateTime.now())
    val topic = Topic(
      createTopic.topic,
      createTopic.nick,
      date,
      date
    )

    db.run {
      for {
        newTopic <- topics returning topics += topic
        newPost <- {
          val post = Post(
            createTopic.content,
            createTopic.nick,
            createTopic.email,
            date,
            UUID.randomUUID().toString,
            newTopic.id
          )

          posts returning posts += post
        }
      } yield Main.TopicPost(newTopic, newPost)
    }
  }

  override def addPost(topicId: Long, createPost: CreatePost): Future[Post] = db.run {
    val date = Timestamp.valueOf(LocalDateTime.now())
    for {
      tid <- topics.filter(_.id === topicId).result.headOption
      newPost <- tid match {
        case Some(_) =>
          val post = Post(
            createPost.content,
            createPost.nick,
            createPost.email,
            date,
            UUID.randomUUID().toString,
            topicId
          )

          topics.filter(_.id === topicId).map(_.last_response).update(date) >>
            (posts returning posts += post)
        case None => DBIO.failed(NoTopic(topicId.toString))
      }
    } yield newPost
  }

  override def updatePost(postSecret: String, updatePost: UpdatePost): Future[Post] = db.run {
    for {
      exists <- posts.filter(_.secret === postSecret).result.headOption
      post <- exists match {
        case Some(_) => posts.filter(_.secret === postSecret).map(_.content).update(updatePost.content) andThen
          posts.filter(_.secret === postSecret).result.head
        case None => DBIO.failed(NoPost(postSecret))
      }
    } yield post
  }

  override def deletePost(postSecret: String): Future[Int] = db.run {
    posts.filter(_.secret === postSecret).delete
  }

  override def getPost(postId: Long): Future[Option[Post]] = db.run {
    posts.filter(_.id === postId).result.headOption
  }

  override def getTopic(topicId: Long): Future[Option[Topic]] = db.run {
    topics.filter(_.id === topicId).result.headOption
  }
}
