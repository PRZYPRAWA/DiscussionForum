package database.queries

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import appConfig.Config
import main._

import scala.math.max

object QueryParameters extends Config {
  val LIMIT = config.getInt("app.limit")
  val AFTER = LIMIT * 2 / 3
  val BEFORE = LIMIT / 3

  type Offset = Int
  type Limit = Int
  type After = Int
  type Before = Int

  def getLimitOffset(limit: Option[Int] = None, offset: Option[Int] = None): (Limit, Offset) = {
    val tmpLimit = limit.getOrElse(0)
    val lim = if (tmpLimit <= 0 || tmpLimit > LIMIT) LIMIT else tmpLimit

    val tmpOffset = offset.getOrElse(0)
    val off = max(0, tmpOffset)

    (lim, off)
  }

  def getOffsetAfterBefore(offset: Option[Int] = None, before: Option[Int] = None, after: Option[Int] = None): (Offset, After, Before) = {
    val tmpBefore = before.getOrElse(0)
    val bef1 = if (tmpBefore < 0 || tmpBefore > BEFORE) BEFORE else tmpBefore

    val tmpAfter = after.getOrElse(0)
    val aft1 = if (tmpAfter < 0 || tmpAfter > AFTER) AFTER else tmpAfter

    val (aft2, bef2) = getAfterBefore(aft1, bef1)

    val tmpOff = offset.getOrElse(0)
    val off1 = max(0, tmpOff)
    val off2 = max(0, off1 - bef2)

    (off2, aft2, bef2)
  }

  private def getAfterBefore(aft: Int, bef: Int): (After, Before) = {
    val sum = aft + bef + 1
    if (sum > LIMIT) (LIMIT * aft / sum, LIMIT * bef / sum)
    else if (aft == 0 && bef == 0) (AFTER, BEFORE)
    else (aft, bef)
  }

  def createTopicPostToAdd(createTopic: CreateTopic): (Topic, Post) = {
    val date = Timestamp.valueOf(LocalDateTime.now())

    val topic = Topic(
      createTopic.topic,
      createTopic.username,
      date,
      date
    )

    val post = Post(
      createTopic.content,
      createTopic.username,
      createTopic.email,
      date,
      TPValues.Secret(UUID.randomUUID().toString)
    )

    (topic, post)
  }

  def createPostToAdd(createPost: CreatePost): Post = {
    val date = Timestamp.valueOf(LocalDateTime.now())

    val post = Post(
      createPost.content,
      createPost.username,
      createPost.email,
      date,
      TPValues.Secret(UUID.randomUUID().toString)
    )

    post
  }
}
