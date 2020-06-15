import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.{Connection, ForumRepository}
import main.{ForumRouter, Post, TopicDirectives, UpdatePost}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import validation.ApiError

class PostInteg extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives {
  val updatePost = UpdatePost("Test update")
  val invalidUpdatePost = UpdatePost("")

  trait DbConnectionTests {
    val connection = new Connection
    val db = new ForumRepository(connection.database)
    val topicRouter = new ForumRouter(db)
  }

  "A PostRouter" should {
    "update post with valid data" in new DbConnectionTests {
      Put("/posts/a8a52c07-1b95-4a7e-8c46-34818e463caa", updatePost) ~> topicRouter.postRoute ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Post]
        resp.content shouldBe updatePost.content
      }
    }

    "not update post with invalid data" in new DbConnectionTests {
      Put("/posts/a8a52c07-1b95-4a7e-8c46-34818e463caa", invalidUpdatePost) ~> topicRouter.postRoute ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.emptyContentField
      }
    }

    "not update post with valid data when there is no post with secret" in new DbConnectionTests {
      val secret = "123"
      Put("/posts/" + secret, updatePost) ~> topicRouter.postRoute ~> check {
        status shouldBe StatusCodes.NotFound
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.postNotFound(secret)
      }
    }

    "return posts" in new DbConnectionTests {
      Get("/posts") ~> topicRouter.postRoute ~> check {
        status shouldBe StatusCodes.OK
      }
    }

  }
}
