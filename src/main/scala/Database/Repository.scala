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
    val bef = if (tmpBefore < 0 || tmpBefore > BEFORE) BEFORE else tmpBefore

    val tmpAfter = after.getOrElse(0)
    val aft = if (tmpAfter < 0 || tmpAfter > AFTER) AFTER else tmpAfter

    val sum = aft + bef + 1
    val (a, b) =
      if (sum > LIMIT) (LIMIT * aft / sum, LIMIT * bef / sum)
      else if (aft == 0 && bef == 0) (AFTER, BEFORE)
      else (aft, bef)

    val tmpOff = offset.getOrElse(0)
    val off = if (tmpOff < 0) 0 else tmpOff
    val o = if (off - b < 0) 0 else off - b

    db.run {
      for {
        t <- {
          topics.filter(_.id === topicId).result.headOption
        }
        ps <- t match {
          case Some(_) => posts
            .filter(_.topic_id === topicId)
            .sortBy(_.created)
            .drop(o)
            .take(a + b + 1)
            .result
          case None => DBIO.failed(NoTopic("topicWithPosts"))
        }
      } yield Main.TopicPosts(t.get, ps)
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
        t <- topics returning topics += topic
        p <- {
          val post = Post(
            createTopic.content,
            createTopic.nick,
            createTopic.email,
            date,
            UUID.randomUUID().toString,
            t.id
          )

          posts returning posts += post
        }
      } yield Main.TopicPost(t, p)
    }
  }

  override def addPost(topicId: Long, createPost: CreatePost): Future[Post] = db.run {
    val date = Timestamp.valueOf(LocalDateTime.now())
    for {
      tid <- topics.filter(_.id === topicId).result.headOption
      p <- tid match {
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
    } yield p
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
