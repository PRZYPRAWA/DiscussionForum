package Database

import java.sql.Timestamp

import Main.{Post, Topic}
import slick.jdbc.PostgresProfile.api._
import slick.sql.SqlProfile.ColumnOption.SqlType

class TopicTable(tag: Tag) extends Table[Topic](tag, "topic") {

  def id = column[Long]("id", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)

  def topic = column[String]("topic")

  def created_by = column[String]("created_by")

  def created = column[Timestamp]("created")

  def last_response = column[Timestamp]("last_response")

  def * = (topic, created_by, created, last_response, id).mapTo[Topic]

}

class PostTable(tag: Tag) extends Table[Post](tag, "post") {

  def id = column[Long]("id", SqlType("SERIAL"), O.PrimaryKey, O.AutoInc)

  def topic_id = column[Long]("topic_id")

  def content = column[String]("content")

  def username = column[String]("username")

  def email = column[String]("email")

  def created = column[Timestamp]("created")

  def secret = column[String]("secret")

  def * = (content, username, email, created, secret, topic_id, id).mapTo[Post]

  def topic_fk = foreignKey("topic", topic_id, TableQuery[TopicTable])(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

}
