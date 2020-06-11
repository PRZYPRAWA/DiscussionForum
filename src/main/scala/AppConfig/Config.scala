package AppConfig

import com.typesafe.config.ConfigFactory

trait Config {
  val config = ConfigFactory.load()
}
