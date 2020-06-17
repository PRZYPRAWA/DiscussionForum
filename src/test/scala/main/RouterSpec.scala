package main

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.ForumRepository.{NoPost, NoTopic}
import database.Repository
import org.scalamock.scalatest.MockFactory
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import validation.ApiError

import scala.concurrent.Future

class RouterSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives with MockFactory with GivenWhenThen {

  trait RepoMock {
    val db = mock[Repository]
    val forumRouter = new ForumRouter(db)

    val dateTest = Timestamp.valueOf(LocalDateTime.now())
    val testId1 = 1L
    val testId2 = 2L
    val invalidId = 3L
    val secretTest1 = "secret1"
    val secretTest2 = "secret2"
    val invalidSecret = "secret2"
    val topicTest = "Mock topic"
    val contentTest = "Mock content"
    val nickTest = "Mock nick"
    val emailTest = "mock@gmail.com"

    val createTopic = CreateDiscussionTopic(topicTest, contentTest, nickTest, emailTest)

    val topic1 = Topic(
      topicTest,
      nickTest,
      dateTest,
      dateTest,
      testId1
    )

    val topic2 = Topic(
      topicTest,
      nickTest,
      dateTest,
      dateTest,
      testId2
    )

    val post1 = main.Post(createTopic.content,
      nickTest,
      emailTest,
      dateTest,
      secretTest1,
      testId1,
      testId1
    )

    val post2 = main.Post(createTopic.content,
      nickTest,
      emailTest,
      dateTest,
      secretTest2,
      testId1,
      testId2
    )

    val createPost = CreatePost(
      contentTest,
      nickTest,
      emailTest
    )

    val topicPosts1 = TopicPosts(topic1, Seq(post1, post2))
    val topicPosts2 = TopicPosts(topic1, Seq(post2))
    val updatePost = UpdatePost("New mock content")
  }

  "A ForumRouter" should {
    "create topic and post with valid data" in new RepoMock {

      db.addTopic _ expects createTopic returns Future.successful(TPValues(topic1, post1))

      Post("/topics", createTopic) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[TopicPost]
        resp.topic shouldBe topic1
        resp.post shouldBe post1
      }
    }

    "not create topic and post with invalid data" in new RepoMock {
      Post("/topics", createTopic.copy(content = "")) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.emptyContentField
      }

      Post("/topics", createTopic.copy(email = "test.com")) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.wrongEmailFormat
      }
    }

    "add post to corresponding topic with valid data" in new RepoMock {

      db.addPost _ expects(testId1, createPost) returns Future.successful(post1)

      Post("/topics/" + testId1, createPost) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Post]
        resp shouldBe post1
      }
    }

    "not add post to corresponding topic with invalid data" in new RepoMock {
      Post("/topics/" + testId1, createPost.copy(content = "")) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.emptyContentField
      }

      Post("/topics/" + testId1, createPost.copy(email = "test.com")) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.wrongEmailFormat
      }
    }

    "not add post when topic id is invalid" in new RepoMock {

      db.addPost _ expects(invalidId, createPost) returns Future.failed(NoTopic(invalidId.toString))

      Post("/topics/" + invalidId, createPost) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.NotFound
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.topicNotFound(invalidId.toString)
      }
    }

    "delete post when secret is found" in new RepoMock {
      db.deletePost _ expects secretTest1 returns Future.successful(1)
      Delete("/posts/" + secretTest1) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "not delete post when secret is not found" in new RepoMock {
      db.deletePost _ expects invalidSecret returns Future.successful(0)
      Delete("/posts/" + invalidSecret) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "return all posts" in new RepoMock {
      When("no parameter is defined")
      db.getPosts _ expects(None, None) returns Future.successful(Seq(post1, post2))
      Get("/posts") ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Post]]
        resp shouldBe Seq(post1, post2)
      }
    }

    "return first post" in new RepoMock {
      When("limit is defined")
      val limit = 1

      db.getPosts _ expects(Some(1), None) returns Future.successful(Seq(post1))

      Get("/posts?limit=" + limit) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Post]]
        resp shouldBe Seq(post1)
      }
    }

    "return second post" in new RepoMock {
      When("limit and offset are defined")
      val limit = 1
      val offset = 1

      db.getPosts _ expects(Some(1), Some(1)) returns Future.successful(Seq(post2))

      Get("/posts?limit=" + limit + "&offset=" + offset) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Post]]
        resp shouldBe Seq(post2)
      }
    }

    "return all topics" in new RepoMock {
      When("no parameter is defined")

      db.getTopics _ expects(None, None) returns Future.successful(Seq(topic1, topic2))

      Get("/topics") ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Topic]]
        resp shouldBe Seq(topic1, topic2)
      }
    }

    "return first topic" in new RepoMock {
      When("limit is defined")

      db.getTopics _ expects(Some(1), None) returns Future.successful(Seq(topic1))

      val limit = 1

      Get("/topics?limit=" + limit) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Topic]]
        resp shouldBe Seq(topic1)
      }
    }

    "return second topic" in new RepoMock {
      When("limit and offset are defined")

      val limit = 1
      val offset = 1

      db.getTopics _ expects(Some(limit), Some(offset)) returns Future.successful(Seq(topic2))

      Get("/topics?limit=" + limit + "&offset=" + offset) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Seq[Topic]]
        resp shouldBe Seq(topic2)
      }
    }

    "return topic with corresponding posts" in new RepoMock {
      When("no parameter is defined")
      And("topic id is valid")

      db.topicWithPosts _ expects(testId1, None, None, None) returns Future.successful(topicPosts1)

      Get("/topics/" + testId1) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[TopicPosts]
        resp shouldBe topicPosts1
      }

      When("offset, before and after are defined")
      And("topic id is valid")

      val offset = 1
      val after = 0
      val before = 0

      db.topicWithPosts _ expects(testId1, Some(offset), Some(before), Some(after)) returns Future.successful(topicPosts2)

      Get(s"/topics/$testId1?offset=$offset&before=$before&after=$after") ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[TopicPosts]
        resp shouldBe topicPosts2
      }
    }

    "not return topic with corresponding posts" in new RepoMock {
      When("offset, before and after are defined")
      And("topic id is invalid")

      val offset = 1
      val after = 0
      val before = 0

      db.topicWithPosts _ expects(invalidId, Some(offset), Some(before), Some(after)) returns Future.failed(NoTopic(invalidId.toString))

      Get(s"/topics/$invalidId?offset=$offset&before=$before&after=$after") ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.NotFound
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.topicNotFound(invalidId.toString)
      }
    }

    "return updated with valid data post" in new RepoMock {

      db.updatePost _ expects(secretTest1, updatePost) returns Future.successful(post1.copy(content = updatePost.content))

      Put(s"/posts/" + secretTest1, updatePost) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Post]
        resp shouldBe post1.copy(content = updatePost.content)
      }
    }

    "not return updated with valid data post" in new RepoMock {
      When("secret is not found")

      db.updatePost _ expects(invalidSecret, updatePost) returns Future.failed(NoPost(invalidSecret))

      Put(s"/posts/" + invalidSecret, updatePost) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.NotFound
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.postNotFound(invalidSecret)
      }
    }

    "not return updated with invalid data post" in new RepoMock {
      val invalidUpdate = UpdatePost("")
      Put(s"/posts/" + invalidSecret, invalidUpdate) ~> forumRouter.route ~> check {
        status shouldBe StatusCodes.BadRequest
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.emptyContentField
      }
    }

  }
}
