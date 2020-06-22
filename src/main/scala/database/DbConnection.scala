package database

import appConfig.Config

class PostgresConnection(implicit val profile: DatabaseProfile) extends Config {

  import profile.profile.api._

  val connectionUrl: String =
    s"${config.getString("database.url")}" +
      s"?user=${config.getString("database.user")}" +
      s"&password=${config.getString("database.password")}"

  def database = Database.forURL(connectionUrl, driver = config.getString("database.driver"))
}

