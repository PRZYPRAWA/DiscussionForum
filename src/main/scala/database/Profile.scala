package database

import slick.jdbc.JdbcProfile

trait DatabaseModule {

  val profile: JdbcProfile

}

class PostgresModule extends DatabaseModule {

  override val profile: JdbcProfile = slick.jdbc.PostgresProfile

}

