package database

import slick.jdbc.JdbcProfile
import appConfig.Config

trait DatabaseModule {

  val profile: JdbcProfile

}

class PG extends DatabaseModule with Config {

  override val profile: JdbcProfile = slick.jdbc.PostgresProfile

}

