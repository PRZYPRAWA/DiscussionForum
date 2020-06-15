package main

//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.server.Directives
//import akka.http.scaladsl.testkit.ScalatestRouteTest
//import database.ForumRepository
//import org.scalamock.scalatest.MockFactory
//import org.scalatest.Ignore
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AnyWordSpec
//import slick.jdbc.PostgresProfile.api._
//
//
//class CreateTopicSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives with MockFactory {
//  val testCreateTopic = CreateDiscussionTopic("Test topic", "Test content", "Test username", "test@email.com")
//
//  "A ForumRouter" should {
//    "create Topic and Post with valid data" in {
//      val connectionMock = mock[Database]
//      val db = new ForumRepository(connectionMock)
//      val topicRouter = new ForumRouter(db)
//
//      Post("/topics", testCreateTopic) ~> topicRouter.topicRoute ~> check {
//        status shouldBe StatusCodes.OK
//        val resp = responseAs[TopicPost]
//        val (newTopic, newPost) = (resp.topic, resp.post)
//
//        newTopic.created_by shouldBe testCreateTopic.nick
//        newPost.username shouldBe testCreateTopic.nick
//        newTopic.topic shouldBe testCreateTopic.topic
//        newPost.content shouldBe testCreateTopic.content
//        newPost.email shouldBe testCreateTopic.email
//      }
//    }
//  }
//
//}
