package main

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import appConfig.Config
import database.queries.Queries
import database.{ForumRepository, DbConnection, PostgresProfile}
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

  implicit val profile = new PostgresProfile

  val conn = new DbConnection
  val queries = new Queries
  val repo = new ForumRepository(conn, queries)
  val router = new ForumRouter(repo)

  Http().bindAndHandle(router.route, config.getString("http.interface"), config.getInt("http.port"))
}
