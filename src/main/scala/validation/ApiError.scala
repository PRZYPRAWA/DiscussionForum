package validation

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

final case class ApiError(statusCode: StatusCode, message: String)

object ApiError {
  def apply(statusCode: StatusCode, message: String): ApiError = new ApiError(statusCode, message)

  val generic: ApiError = new ApiError(StatusCodes.InternalServerError, "Unknown error.")

  val emptyTopicField: ApiError = new ApiError(StatusCodes.BadRequest, "The topic field must not be empty.")

  val topicTooLong: ApiError = new ApiError(StatusCodes.BadRequest, "The topic field is too long.")

  val emptyContentField: ApiError = new ApiError(StatusCodes.BadRequest, "The content field must not be empty.")

  val contentTooLong: ApiError = new ApiError(StatusCodes.BadRequest, "The content field is too long.")

  val wrongEmailFormat: ApiError = new ApiError(StatusCodes.BadRequest, "The mail field is invalid.")

  val emptyUsernameField: ApiError = new ApiError(StatusCodes.BadRequest, "The username field must not be empty.")

  val wrongUsernameFormat: ApiError = new ApiError(StatusCodes.BadRequest, "The username field is invalid.")

  val usernameTooLong: ApiError = new ApiError(StatusCodes.BadRequest, "The username field is too long.")


  def topicNotFound(id: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The topic with id $id could not be found.")

  def postNotFound(secret: String): ApiError =
    new ApiError(StatusCodes.NotFound, s"The post with secret $secret could not be found.")
}
