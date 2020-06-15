package main

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import appConfig.Config
import database.{Connection, ForumRepository}
import validation._

import scala.concurrent.ExecutionContextExecutor

trait Service extends Protocols with TopicDirectives with ValidatorDirectives {
  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  val logger: LoggingAdapter
}

object ForumMain extends App with Service with Config {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val logger = Logging(system, getClass)

  val connection = new Connection
  val database = new ForumRepository(connection.database)
  val router = new ForumRouter(database)

  Http().bindAndHandle(router.route, config.getString("http.interface"), config.getInt("http.port"))
}
