package database

import appConfig.Config
import database.queries.QueryParameters
import main.{CreatePost, CreateTopic}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class QueryParametersSpec extends AnyWordSpec with Matchers with Config {
  val LIMIT = QueryParameters.LIMIT
  val AFTER = QueryParameters.AFTER
  val BEFORE = QueryParameters.BEFORE

  "A QueryParameters.getLimitOffset" should {
    "return LIMIT and 0 as offset when parameters are not provided" in {
      val (lim, off) = QueryParameters.getLimitOffset()
      lim shouldBe LIMIT
      off shouldBe 0
    }

    "return provided parameters when limit is less than LIMIT" in {
      val (testLimit, testOffset) = (Some(3), Some(3))
      val (lim, off) = QueryParameters.getLimitOffset(testLimit, testOffset)
      lim shouldBe testLimit.get
      off shouldBe testOffset.get
    }

    "return LIMIT and provided offset when passed limit is too much" in {
      val (testLimit, testOffset) = (Some(LIMIT + 1), Some(3))
      val (lim, off) = QueryParameters.getLimitOffset(testLimit, testOffset)
      lim shouldBe LIMIT
      off shouldBe testOffset.get
    }

    "return LIMIT and provided offset when passed limit is negative" in {
      val (testLimit, testOffset) = (Some(-10), Some(3))
      val (lim, off) = QueryParameters.getLimitOffset(testLimit, testOffset)
      lim shouldBe LIMIT
      off shouldBe testOffset.get
    }

    "return LIMIT and provided offset when passed limit is 0" in {
      val (testLimit, testOffset) = (Some(0), Some(3))
      val (lim, off) = QueryParameters.getLimitOffset(testLimit, testOffset)
      lim shouldBe LIMIT
      off shouldBe testOffset.get
    }
  }

  "A QueryParameters.getOffsetAfterBefore" should {
    "return 0, AFTER and BEFORE when parameters are not provided" in {
      val (off, aft, bef) = QueryParameters.getOffsetAfterBefore()
      off shouldBe 0
      aft shouldBe AFTER
      bef shouldBe BEFORE
    }

    "return offset, after and before when parameters are provided and sum is less than LIMIT" in {
      val (offset, after, before) = (Some(5), Some(2), Some(3))
      val (off, aft, bef) = QueryParameters.getOffsetAfterBefore(offset, before, after)
      off shouldBe offset.get - before.get
      aft shouldBe after.get
      bef shouldBe before.get
    }

    "return offset, AFTER and BEFORE when parameters are negative" in {
      val (offset, after, before) = (Some(5), Some(-2), Some(-3))
      val (off, aft, bef) = QueryParameters.getOffsetAfterBefore(offset, before, after)
      off shouldBe scala.math.max(0, off - BEFORE)
      aft shouldBe AFTER
      bef shouldBe BEFORE
    }

    "return offset, AFTER and BEFORE when sum is greater than LIMIT" in {
      val (offset, after, before) = (Some(5), Some(AFTER), Some(BEFORE))
      val (off, aft, bef) = QueryParameters.getOffsetAfterBefore(offset, before, after)
      off shouldBe scala.math.max(0, off - BEFORE)
      aft shouldBe AFTER
      bef shouldBe BEFORE
    }
  }

  "A QueryParameters.createTopicPostToAdd" should {
    import main.TPValuesImplicits._
    "return topic and post with spicified attributes" in {
      val createTopic = CreateTopic("Test topic".toTopic, "Test content".toContent, "Test username".toUsername, "Test email".toEmail)

      val (topic, post) = QueryParameters.createTopicPostToAdd(createTopic)
      topic.topic shouldBe createTopic.topic
      topic.created_by shouldBe createTopic.username

      post.content shouldBe createTopic.content
      post.username shouldBe createTopic.username
      post.email shouldBe createTopic.email
    }
  }

  "A QueryParameters.createPostToAdd" should {
    import main.TPValuesImplicits._
    "return topic and post with spicified attributes" in {
      val createPost = CreatePost("Test content".toContent, "Test username".toUsername, "Test email".toEmail)
      val createdPost = QueryParameters.createPostToAdd(createPost)

      createdPost.content shouldBe createPost.content
      createdPost.username shouldBe createPost.username
      createdPost.email shouldBe createPost.email
    }
  }
}
