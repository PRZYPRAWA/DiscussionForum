import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.queries.Queries
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import database.{ForumRepository, DbConnection, PostgresProfile}
import main.{CreatePost, Deleted, ForumRouter, Post, TopicDirectives, UpdatePost}
import validation.ApiError
import main.TPValuesImplicits._

class PostInteg extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives {
  val updatePost = UpdatePost("Test update".toContent)
  val invalidUpdatePost = UpdatePost("".toContent)

  trait DbConnectionTests {
    implicit val profile = new PostgresProfile

    val conn = new DbConnection
    val queries = new Queries
    val repo = new ForumRepository(conn, queries)
    val postRouter = new ForumRouter(repo)
  }

  "A PostRouter" should {
    "update post with valid data" in new DbConnectionTests {
      Put("/posts/a8a52c07-1b95-4a7e-8c46-34818e463caa", updatePost) ~> postRouter.postRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Post]
        resp.content shouldBe updatePost.content
      }
    }

    "not update post with invalid data" in new DbConnectionTests {
      Put("/posts/a8a52c07-1b95-4a7e-8c46-34818e463caa", invalidUpdatePost) ~> postRouter.postRoute ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.emptyContentField
      }
    }

    "not update post with valid data when there is no post with secret" in new DbConnectionTests {
      val secret = "123"
      Put("/posts/" + secret, updatePost) ~> postRouter.postRoute ~> check {
        status shouldBe StatusCodes.NotFound
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.postNotFound(secret)
      }
    }

    "return posts" in new DbConnectionTests {
      Get("/posts") ~> postRouter.postRoute ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "add and delete post to corresponding topic with valid data" in new DbConnectionTests {
      val testCreatePost = CreatePost("Test content".toContent, "test username".toUsername, "test@email.om".toEmail)
      val deleted = Deleted(1)

      Post("/topics/1", testCreatePost) ~> postRouter.topicRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Post]

        resp.username shouldBe testCreatePost.username
        resp.content shouldBe testCreatePost.content
        resp.email shouldBe testCreatePost.email

        val secret = resp.secret.value

        Delete("/posts/" + secret) ~> postRouter.postRoute ~> check {
          status shouldBe StatusCodes.OK
          val resp = responseAs[Deleted]
          resp shouldBe deleted
        }
      }
    }

    "not delete post when not found" in new DbConnectionTests {
      val invalidSecret = "abc"
      Delete("/posts/" + invalidSecret) ~> postRouter.postRoute ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

  }
}
