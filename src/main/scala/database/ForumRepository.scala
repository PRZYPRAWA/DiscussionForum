package database

import main.{CreatePost, CreateTopic, Post, TPValues, Topic, TopicPost, TopicPosts, UpdatePost}

import scala.concurrent.Future

class ForumRepository(val connection: DbConnection, val queries: Queries) extends Repository {
  val database = connection.database

  override def getTopics(limit: Option[Int], offset: Option[Int]): Future[Seq[Topic]] =
    database.run(queries.getTopics(limit, offset))

  override def getPosts(limit: Option[Int], offset: Option[Int]): Future[Seq[Post]] =
    database.run(queries.getPosts(limit, offset))

  override def getPost(postId: TPValues.Id): Future[Option[Post]] =
    database.run(queries.getPost(postId))

  override def getTopic(topicId: TPValues.Id): Future[Option[Topic]] =
    database.run(queries.getTopic(topicId))

  override def topicWithPosts(topicId: TPValues.Id,
                              offset: Option[Int],
                              before: Option[Int],
                              after: Option[Int]
                             ): Future[TopicPosts] =
    database.run(queries.topicWithPosts(topicId, offset, before, after))

  override def addTopic(createTopic: CreateTopic): Future[TopicPost] = database.run(queries.addTopic(createTopic))

  override def addPost(topicId: TPValues.Id, createPost: CreatePost): Future[Post] =
    database.run(queries.addPost(topicId, createPost))

  override def updatePost(postSecret: TPValues.Secret, updatePost: UpdatePost): Future[Post] =
    database.run(queries.updatePost(postSecret, updatePost))

  override def deletePost(postSecret: TPValues.Secret): Future[Int] =
    database.run(queries.deletePost(postSecret))
}
