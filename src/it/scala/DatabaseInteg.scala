import database.queries.Queries
import database.queries.Queries.{NoPost, NoTopic}
import database.{DbConnection, ForumRepository, PostgresProfile}
import main.TPValuesImplicits._
import main.{CreatePost, CreateTopic, UpdatePost}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

class DatabaseInteg extends AnyWordSpec with Matchers {
  val testCreateTopic = CreateTopic("Test topic".toTopic, "Test content".toContent, "Test username".toUsername, "test@email.com".toEmail)
  val testCreatePost = CreatePost("Test content".toContent, "test username".toUsername, "test@email.om".toEmail)

  trait DatabaseConn {
    implicit val profile = new PostgresProfile

    val conn = new DbConnection
    val queries = new Queries
    val repo = new ForumRepository(conn, queries)
  }

  "A database" should {
    "add and return new Topic with Post" in new DatabaseConn {
      val resp = repo.addTopic(testCreateTopic)

      resp map { result =>
        val topic = result.topic
        val post = result.post

        topic.topic shouldBe testCreateTopic.topic
        post.content shouldBe testCreateTopic.content
      }
    }

    "return topics" in new DatabaseConn {
      val (limit, offset) = (Some(3), Some(2))
      val resp = repo.getTopics(limit, offset)

      resp map { topics =>
        assert(topics.length <= limit.get)
      }
    }

    "return posts" in new DatabaseConn {
      val (limit, offset) = (Some(3), Some(2))
      val resp = repo.getPosts(limit, offset)

      resp map { topics =>
        assert(topics.length <= limit.get)
      }
    }

    "return topic with posts" in new DatabaseConn {
      val topicId = 1L
      val (offset, before, after) = (Some(3), Some(2), Some(4))
      val resp = repo.topicWithPosts(topicId.toId, offset, before, after)

      resp map { tp =>
        val topic = tp.topic
        val posts = tp.posts

        topic.id shouldBe topicId
        if (posts.nonEmpty) {
          posts.head.topic_id shouldBe topicId
          assert(posts.length <= before.get + after.get)
        }
      }
    }

    "not return topic with posts when invalid id" in new DatabaseConn {
      val topicId = 0L.toId
      val (offset, before, after) = (Some(3), Some(2), Some(4))
      val resp = repo.topicWithPosts(topicId, offset, before, after)

      resp onComplete {
        case Failure(f) => assert(f == NoTopic(topicId))
      }
    }


    "add and return new post" in new DatabaseConn {
      val newTopic = repo.addTopic(testCreateTopic)

      newTopic.map { topicResult =>
        val tid = topicResult.topic.id

        val newPost = repo.addPost(tid, testCreatePost)

        newPost.map { postResult =>
          postResult.topic_id shouldBe tid

          postResult.content shouldBe testCreatePost.content
          postResult.username shouldBe testCreatePost.username
          postResult.email shouldBe testCreatePost.email
        }
      }
    }

    "not add new post when invalid id" in new DatabaseConn {
      val invalidId = 0.toId
      val newPost = repo.addPost(invalidId, testCreatePost)

      newPost.onComplete {
        case Failure(f) => assert(f == NoTopic(invalidId))
      }
    }

    "update post" in new DatabaseConn {
      val newTopic = repo.addTopic(testCreateTopic)

      newTopic.map { topicResult =>
        val tid = topicResult.topic.id

        val newPost = repo.addPost(tid, testCreatePost)

        newPost.map { postResult =>
          postResult.topic_id shouldBe tid

          postResult.content shouldBe testCreatePost.content
          postResult.username shouldBe testCreatePost.username
          postResult.email shouldBe testCreatePost.email

          val updatePost = UpdatePost("New content".toContent)

          val updatedPost = repo.updatePost(postResult.secret, updatePost)
          updatedPost map { updateResult =>
            updateResult.content shouldBe updatePost.content
          }
        }
      }
    }

    "not update post" in new DatabaseConn {
      val invalidSecret = "abc".toSecret

      val updatePost = UpdatePost("New content".toContent)

      val updatedPost = repo.updatePost(invalidSecret, updatePost)
      updatedPost onComplete {
        case Failure(f) => assert(f == NoPost(invalidSecret))
      }
    }

    "delete post" in new DatabaseConn {
      val newTopic = repo.addTopic(testCreateTopic)

      newTopic.map { topicResult =>
        val tid = topicResult.topic.id

        val newPost = repo.addPost(tid, testCreatePost)

        newPost.map { postResult =>
          postResult.topic_id shouldBe tid

          postResult.content shouldBe testCreatePost.content
          postResult.username shouldBe testCreatePost.username
          postResult.email shouldBe testCreatePost.email

          val deletedPost = repo.deletePost(postResult.secret)
          deletedPost map { deleteResult =>
            deleteResult.rows shouldBe 1
          }
        }
      }
    }

    "not delete post" in new DatabaseConn {
      val invalidSecret = "123".toSecret

      val deletedPost = repo.deletePost(invalidSecret)
      deletedPost onComplete {
        case Failure(f) => assert(f == NoPost(invalidSecret))
      }
    }

  }
}
