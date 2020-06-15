package main

import validation.ApiError
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.{Directive1, Directives}
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait TopicDirectives extends Directives with Protocols {

  def handle[T](f: Future[T])(e: Throwable => ApiError): Directive1[T] = onComplete(f) flatMap {
    case Success(t) =>
      provide(t)
    case Failure(error) =>
      val apiError = e(error)
      complete(HttpResponse(apiError.statusCode, entity = HttpEntity(ContentTypes.`application/json`, apiError.toJson.toString)))
  }

  def handleWithGeneric[T](f: Future[T]): Directive1[T] =
    handle[T](f)(_ => ApiError.generic)

}
