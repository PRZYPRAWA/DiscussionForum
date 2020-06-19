import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.{DbConnection, ForumRepository, PG, Queries}
import main._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import validation.ApiError
import TPValuesImplicits._

class TopicInteg extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives {
  val testCreateTopic = CreateTopic("Test topic".toTopic, "Test content".toContent, "Test username".toUsername, "test@email.com".toEmail)
  val invalidTestCreateTopic = CreateTopic("Test topic".toTopic, "Test content".toContent, "Test username".toUsername, "email.com".toEmail)

  val testCreatePost = CreatePost("Test content".toContent, "test username".toUsername, "test@email.om".toEmail)
  val invalidTestCreatePost = CreatePost("Test content".toContent, "test username".toUsername, "test.com".toEmail)

  private var postToDelete: String = ""

  trait DbConnectionTests {
    implicit val profile = new PG

    val conn = new DbConnection
    val queries = new Queries
    val repo = new ForumRepository(conn, queries)
    val topicRouter = new ForumRouter(repo)
  }

  "A TopicRouter" should {
    "add topic and post with valid data and return them" in new DbConnectionTests {
      Post("/topics", testCreateTopic) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[TopicPost]
        val (newTopic, newPost) = (resp.topic, resp.post)

        postToDelete = newPost.secret.value

        newTopic.created_by shouldBe testCreateTopic.username
        newPost.username shouldBe testCreateTopic.username
        newTopic.topic shouldBe testCreateTopic.topic
        newPost.content shouldBe testCreateTopic.content
        newPost.email shouldBe testCreateTopic.email
      }
    }

    "delete post with secret" in new DbConnectionTests {
      Delete("/posts/" + postToDelete) ~> topicRouter.postRoute ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "not add topic and post with invalid data" in new DbConnectionTests {
      Post("/topics", invalidTestCreateTopic) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.wrongEmailFormat
      }
    }

    "add post to corresponding topic with valid data and return them" in new DbConnectionTests {
      Post("/topics/1", testCreatePost) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Post]

        postToDelete = resp.secret.value

        resp.username shouldBe testCreatePost.username
        resp.content shouldBe testCreatePost.content
        resp.email shouldBe testCreatePost.email
      }
    }

    "delete another post with secret" in new DbConnectionTests {
      Delete("/posts/" + postToDelete) ~> topicRouter.postRoute ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "not add post to corresponding topic with invalid data" in new DbConnectionTests {
      Post("/topics/1", invalidTestCreatePost) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.wrongEmailFormat
      }
    }

    "return topic and corresponding posts" in new DbConnectionTests {
      Get("/topics/1") ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[TopicPosts]
        assert(resp.posts.nonEmpty)
        resp.topic.id shouldBe resp.posts.head.topic_id
      }
    }

    "return topics" in new DbConnectionTests {
      Get("/topics") ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Topic]]
        assert(resp.nonEmpty)
      }
    }

    "not return topic and corresponding posts when invalid id" in new DbConnectionTests {
      val topicId = Long.MaxValue.toString
      Get("/topics/" + topicId) ~> topicRouter.topicRoute ~> check {
        status shouldBe StatusCodes.NotFound
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.topicNotFound(topicId)
      }
    }

  }
}
