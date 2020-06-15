package main

import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import validation.ApiError

class TopicDirectivesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with Directives with TopicDirectives {

  private val testRoute = pathPrefix("test") {
    path("success") {
      get {
        handleWithGeneric(Future.unit) { _ =>
          complete(StatusCodes.OK)
        }
      }
    } ~ path("failure") {
      get {
        handleWithGeneric(Future.failed(new Exception("Failure!"))) { _ =>
          complete(StatusCodes.OK)
        }
      }
    }
  }

  "TopicDirectives" should {

    "not return an error if the future succeeds" in {
      Get("/test/success") ~> testRoute ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "return an error if the future fails" in {
      Get("/test/failure") ~> testRoute ~> check {
        status shouldBe StatusCodes.InternalServerError
        val resp = responseAs[ApiError]
        resp shouldBe ApiError.generic
      }
    }
  }
}
