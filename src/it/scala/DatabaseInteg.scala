import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scala.concurrent.ExecutionContext.Implicits.global
import database.{DbConnection, ForumRepository, PG, Queries}
import main.{CreatePost, CreateTopic}
import main.TPValuesImplicits._

class DatabaseInteg extends AnyWordSpec with Matchers {

  val testCreateTopic = CreateTopic("Test topic".toTopic, "Test content".toContent, "Test username".toUsername, "test@email.com".toEmail)
  val invalidTestCreateTopic = CreateTopic("Test topic".toTopic, "Test content".toContent, "Test username".toUsername, "email.com".toEmail)

  val testCreatePost = CreatePost("Test content".toContent, "test username".toUsername, "test@email.om".toEmail)
  val invalidTestCreatePost = CreatePost("Test content".toContent, "test username".toUsername, "test.com".toEmail)

  trait DatabaseConn {
    implicit val profile = new PG

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

  }

}
