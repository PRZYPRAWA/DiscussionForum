package database

import database.queries.Queries
import database.queries.QueryParameters._
import scala.concurrent.Future
import main.{CreatePost, CreateTopic, Deleted, Post, TPValues, Topic, TopicPost, TopicPosts, UpdatePost}

trait Repository {

  def getTopics(limit: Option[Int], offset: Option[Int]): Future[Seq[Topic]]

  def getPosts(limit: Option[Int], offset: Option[Int]): Future[Seq[Post]]

  def getPost(postId: TPValues.Id): Future[Option[Post]]

  def getTopic(topicId: TPValues.Id): Future[Option[Topic]]

  def topicWithPosts(topicId: TPValues.Id, offset: Option[Int], before: Option[Int], after: Option[Int]): Future[TopicPosts]

  def addTopic(createTopic: CreateTopic): Future[TopicPost]

  def addPost(topicId: TPValues.Id, createPost: CreatePost): Future[Post]

  def updatePost(postSecret: TPValues.Secret, updatePost: UpdatePost): Future[Post]

  def deletePost(postSecret: TPValues.Secret): Future[Deleted]
}

class ForumRepository(val connection: DbConnection, val queries: Queries) extends Repository {
  val database = connection.database

  override def getTopics(limit: Option[Int], offset: Option[Int]): Future[Seq[Topic]] = {
    val (lim, off) = getLimitOffset(limit, offset)
    database.run(queries.getTopics(lim, off))
  }

  override def getPosts(limit: Option[Int], offset: Option[Int]): Future[Seq[Post]] = {
    val (lim, off) = getLimitOffset(limit, offset)
    database.run(queries.getPosts(lim, off))
  }

  override def getPost(postId: TPValues.Id): Future[Option[Post]] =
    database.run(queries.getPost(postId))


  override def getTopic(topicId: TPValues.Id): Future[Option[Topic]] =
    database.run(queries.getTopic(topicId))

  override def topicWithPosts(topicId: TPValues.Id,
                              offset: Option[Int],
                              before: Option[Int],
                              after: Option[Int]
                             ): Future[TopicPosts] = {
    val (off, aft, bef) = getOffsetAfterBefore(offset, after, before)
    database.run(queries.topicWithPosts(topicId, off, bef, aft))
  }

  override def addTopic(createTopic: CreateTopic): Future[TopicPost] = {
    val (t, p) = createTopicPostToAdd(createTopic)
    database.run(queries.addTopic(t, p))
  }

  override def addPost(topicId: TPValues.Id, createPost: CreatePost): Future[Post] = {
    val post = createPostToAdd(createPost)
    database.run(queries.addPost(topicId, post))
  }

  override def updatePost(postSecret: TPValues.Secret, updatePost: UpdatePost): Future[Post] =
    database.run(queries.updatePost(postSecret, updatePost))

  override def deletePost(postSecret: TPValues.Secret): Future[Deleted] =
    database.run(queries.deletePost(postSecret))
}
