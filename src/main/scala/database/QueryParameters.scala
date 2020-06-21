package database

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID

import appConfig.Config
import main.{CreatePost, CreateTopic, Post, TPValues, Topic}

import scala.math.max

object QueryParameters {

  type Offset = Int
  type Limit = Int
  type After = Int
  type Before = Int

  object ParamConsts extends Config {
    val LIMIT = config.getInt("app.limit")
    val AFTER = LIMIT * 2 / 3
    val BEFORE = LIMIT / 3
  }

  def getLimitOffset(limit: Option[Int] = None, offset: Option[Int] = None): (Offset, Limit) = {
    val tmpLimit = limit.getOrElse(0)
    val lim = if (tmpLimit <= 0 || tmpLimit > ParamConsts.LIMIT) ParamConsts.LIMIT else tmpLimit


    val tmpOffset = offset.getOrElse(0)
    val off = max(0, tmpOffset)

    (off, lim)
  }

  def getOffsetAfterBefore(offset: Option[Int] = None, before: Option[Int] = None, after: Option[Int] = None): (Offset, After, Before) = {
    val tmpBefore = before.getOrElse(0)
    val bef1 = if (tmpBefore < 0 || tmpBefore > ParamConsts.BEFORE) ParamConsts.BEFORE else tmpBefore


    val tmpAfter = after.getOrElse(0)
    val aft1 = if (tmpAfter < 0 || tmpAfter > ParamConsts.AFTER) ParamConsts.AFTER else tmpAfter

    val (aft2, bef2) = getAfterBefore(aft1, bef1)

    val tmpOff = offset.getOrElse(0)
    val off1 = max(0, tmpOff)
    val off2 = max(0, off1 - bef2)

    (off2, aft2, bef2)
  }

  private def getAfterBefore(aft: Int, bef: Int): (Int, Int) = {
    val sum = aft + bef + 1
    if (sum > ParamConsts.LIMIT) (ParamConsts.LIMIT * aft / sum, ParamConsts.LIMIT * bef / sum)
    else if (aft == 0 && bef == 0) (ParamConsts.AFTER, ParamConsts.BEFORE)
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

  def createPostToAdd(createPost: CreatePost) = {
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
