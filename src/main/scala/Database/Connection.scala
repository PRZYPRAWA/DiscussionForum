package Database

import AppConfig.Config
import slick.jdbc.PostgresProfile.api._


trait Connection extends Config {
  val connectionUrl: String =
    s"${config.getString("database.url")}" +
    s"?user=${config.getString("database.user")}" +
    s"&password=${config.getString("database.password")}"

  val db = Database.forURL(connectionUrl, driver = config.getString("database.driver"))
}
