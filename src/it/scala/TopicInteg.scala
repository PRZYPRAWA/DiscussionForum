import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.{Connection, ForumRepository}
import main.{CreateDiscussionTopic, CreatePost, ForumRouter, Post, Topic, TopicDirectives, TopicPost, TopicPosts}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import validation.ApiError

class TopicInteg extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives with BeforeAndAfterEach {
  val testCreateTopic = CreateDiscussionTopic("Test topic", "Test content", "Test username", "test@email.com")
  val invalidTestCreateTopic = CreateDiscussionTopic("Test topic", "Test content", "Test username", "email.com")

  val testCreatePost = CreatePost("Test content", "test username", "test@email.om")
  val invalidTestCreatePost = CreatePost("Test content", "test username", "test.com")

  trait Tests {
    val connection = new Connection
    val db = new ForumRepository(connection.database)
    val topicRouter = new ForumRouter(db)
  }

  "A TopicRouter" should {
    "add topic and post with valid data and return them" in new Tests {
      Post("/topics", testCreateTopic) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[TopicPost]
        val (newTopic, newPost) = (resp.topic, resp.post)

        newTopic.created_by shouldBe testCreateTopic.nick
        newPost.username shouldBe testCreateTopic.nick
        newTopic.topic shouldBe testCreateTopic.topic
        newPost.content shouldBe testCreateTopic.content
        newPost.email shouldBe testCreateTopic.email
      }
    }

    "not add topic and post with invalid data" in new Tests {
      Post("/topics", invalidTestCreateTopic) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.wrongEmailFormat
      }
    }

    "add post to corresponding topic with valid data and return them" in new Tests {
      Post("/topics/1", testCreatePost) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Post]

        resp.username shouldBe testCreatePost.nick
        resp.content shouldBe testCreatePost.content
        resp.email shouldBe testCreatePost.email
      }
    }

    "not add post to corresponding topic with invalid data" in new Tests {
      Post("/topics/1", invalidTestCreatePost) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.wrongEmailFormat
      }
    }

    "return topic and corresponding posts" in new Tests {
      Get("/topics/1") ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[TopicPosts]
        assert(resp.posts.nonEmpty)
        resp.topic.id shouldBe resp.posts.head.topic_id
        resp.topic.created_by shouldBe resp.posts.head.username
      }
    }

    "return topics" in new Tests {
      Get("/topics") ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Topic]]
        assert(resp.nonEmpty)
      }
    }

    "not return topic and corresponding posts when invalid id" in new Tests {
      val topicId = Long.MaxValue.toString
      Get("/topics/" + topicId) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.NotFound
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.topicNotFound(topicId)
      }
    }

  }
}
