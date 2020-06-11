package Validation

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

final case class ApiError private(statusCode: StatusCode, message: String)

object ApiError {
  private def apply(statusCode: StatusCode, message: String): ApiError = new ApiError(statusCode, message)

  val generic: ApiError = new ApiError(StatusCodes.InternalServerError, "Unknown error.")

  val emptyTopicField: ApiError = new ApiError(StatusCodes.BadRequest, "The topic field must not be empty.")

  val emptyContentField: ApiError = new ApiError(StatusCodes.BadRequest, "The content field must not be empty.")

  val emptyUsernameField: ApiError = new ApiError(StatusCodes.BadRequest, "The username field must not be empty.")

  val emptyEmailField: ApiError = new ApiError(StatusCodes.BadRequest, "The email field must not be empty.")

  def topicNotFound(id: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The topic with id $id could not be found.")

  def postNotFound(secret: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The post with secret $secret could not be found.")
}
