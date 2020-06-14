package Validation

import Main.Protocols
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.{Directive0, Directives}
import spray.json._

trait ValidatorDirectives extends Directives with Protocols {

  def validateWith[T](validator: Validator[T])(t: T): Directive0 =
    validator.validate(t) match {
      case Some(apiError) =>
        complete(HttpResponse(apiError.statusCode, entity = HttpEntity(ContentTypes.`application/json`, apiError.toJson.toString)))
      case None =>
        pass
    }

}
