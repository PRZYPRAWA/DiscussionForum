package database

import appConfig.Config
import slick.jdbc.PostgresProfile.api._


class Connection extends Config {
  val connectionUrl: String =
    s"${config.getString("database.url")}" +
    s"?user=${config.getString("database.user")}" +
    s"&password=${config.getString("database.password")}"

  def database = Database.forURL(connectionUrl, driver = config.getString("database.driver"))
}
