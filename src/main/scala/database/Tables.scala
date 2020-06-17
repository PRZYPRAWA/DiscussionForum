package database

import java.sql.Timestamp


import main.{Post, TPValues, Topic}
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

class TopicTable(tag: Tag) extends Table[Topic](tag, "topic") {

  def id = column[TPValues.Id]("id", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)

  def topic = column[TPValues.Topic]("topic")

  def created_by = column[TPValues.Username]("created_by")

  def created = column[Timestamp]("created")

  def last_response = column[Timestamp]("last_response")

  def * = (topic, created_by, created, last_response, id).mapTo[Topic]

}

class PostTable(tag: Tag) extends Table[Post](tag, "post") {

  def id = column[TPValues.Id]("id", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)

  def topic_id = column[TPValues.Id]("topic_id")

  def content = column[TPValues.Content]("content")

  def username = column[TPValues.Username]("username")

  def email = column[TPValues.Email]("email")

  def created = column[Timestamp]("created")

  def secret = column[TPValues.Secret]("secret")

  def * = (content, username, email, created, secret, topic_id, id).mapTo[Post]

  def topic_fk = foreignKey("topic", topic_id, TableQuery[TopicTable])(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

}
