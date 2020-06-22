package database

import slick.jdbc.JdbcProfile

trait DatabaseProfile {

  val profile: JdbcProfile

}

class PostgresProfile extends DatabaseProfile {

  override val profile: JdbcProfile = slick.jdbc.PostgresProfile

}

