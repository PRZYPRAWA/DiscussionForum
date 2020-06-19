import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.{DbConnection, ForumRepository, PG, Queries}
import main.{ForumRouter, Post, TopicDirectives, UpdatePost}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import validation.ApiError
import main.TPValuesImplicits._

class PostInteg extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives {
  val updatePost = UpdatePost("Test update".toContent)
  val invalidUpdatePost = UpdatePost("".toContent)

  trait DbConnectionTests {
    implicit val profile = new PG

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

  }
}
