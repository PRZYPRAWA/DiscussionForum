package database

import appConfig.Config
import slick.jdbc.JdbcProfile

trait DatabaseModule {

  val profile: JdbcProfile

}

class PG extends DatabaseModule with Config {

  override val profile: JdbcProfile = slick.jdbc.PostgresProfile

}

class H2 extends DatabaseModule with Config {

  override val profile: JdbcProfile = slick.jdbc.H2Profile

}