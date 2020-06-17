package validation

import main.{CreateTopic, CreatePost, UpdatePost}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ValidatorSpec extends AnyWordSpec with Matchers {
  "ValidatorConsts" should {
    "return None if email is valid" in {
      val validEmail1 = "test@wp.pl"
      val validEmail2 = "test@wp.com.pl"

      val validatedEmail1 = ValidatorConsts.validateEmail(validEmail1)
      val validatedEmail2 = ValidatorConsts.validateEmail(validEmail2)

      validatedEmail1 shouldBe None
      validatedEmail2 shouldBe None
    }

    "return Some(ApiError.wrongEmailFormat) if email is invalid" in {
      val invalidEmail1 = "@wp.pl"
      val invalidEmail2 = ""

      val validatedEmail1 = ValidatorConsts.validateEmail(invalidEmail1)
      val validatedEmail2 = ValidatorConsts.validateEmail(invalidEmail2)

      validatedEmail1 shouldBe Some(ApiError.wrongEmailFormat)
      validatedEmail2 shouldBe Some(ApiError.wrongEmailFormat)
    }

    "return None if content is valid" in {
      val validContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas efficitur augue sed est " +
        "aliquet dictum. Praesent vitae nisl eget est."

      val validatedContent = ValidatorConsts.validateContent(validContent)

      validatedContent shouldBe None
    }

    "return Some(ApiError) if content is invalida" in {
      val validContent = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas efficitur augue sed est " +
        "aliquet dictum. Praesent vitae nisl eget est."
      val invalidContent1 = validContent * 100
      val invalidContent2 = ""

      val validatedContent1 = ValidatorConsts.validateContent(invalidContent1)
      val validatedContent2 = ValidatorConsts.validateContent(invalidContent2)

      validatedContent1 shouldBe Some(ApiError.contentTooLong)
      validatedContent2 shouldBe Some(ApiError.emptyContentField)
    }

    "return None if username is valid" in {
      val validUsername = "test!!!._123"

      val validatedUsername = ValidatorConsts.validateUsername(validUsername)

      validatedUsername shouldBe None
    }

    "return Some(ApiError) if username is invalid" in {
      val invalidUsername1 = "a" * 51
      val invalidUsername2 = ""

      val validatedUsername1 = ValidatorConsts.validateUsername(invalidUsername1)
      val validatedUsername2 = ValidatorConsts.validateUsername(invalidUsername2)

      validatedUsername1 shouldBe Some(ApiError.usernameTooLong)
      validatedUsername2 shouldBe Some(ApiError.emptyUsernameField)
    }

    "return None if topic is valid" in {
      val validTopic = "Test"

      val validatedTopic = ValidatorConsts.validateTopic(validTopic)

      validatedTopic shouldBe None
    }

    "return Some(ApiError) if topic is valid" in {
      val validTopic1 = ""
      val validTopic2 = "Test" * 51

      val validatedTopic1 = ValidatorConsts.validateTopic(validTopic1)
      val validatedTopic2 = ValidatorConsts.validateTopic(validTopic2)

      validatedTopic1 shouldBe Some(ApiError.emptyTopicField)
      validatedTopic2 shouldBe Some(ApiError.topicTooLong)
    }
  }

  "CreateTopicValidator" should {
    "return None if CreateTopic is valid" in {
      val createTopic = CreateDiscussionTopic("test", "test", "test", "test@wp.pl")

      val validatedCreateTopic = CreateTopicValidator.validate(createTopic)

      validatedCreateTopic shouldBe None
    }

    "return Some(ApiError) if CreateTopic is invalid" in {
      val createTopic1 = CreateDiscussionTopic("", "test", "test", "test@wp.pl")
      val createTopic2 = CreateDiscussionTopic("test", "", "test", "test@wp.pl")
      val createTopic3 = CreateDiscussionTopic("test", "test", "", "test@wp.pl")
      val createTopic4 = CreateDiscussionTopic("test", "test", "test", "")

      val validatedCreateTopic1 = CreateTopicValidator.validate(createTopic1)
      val validatedCreateTopic2 = CreateTopicValidator.validate(createTopic2)
      val validatedCreateTopic3 = CreateTopicValidator.validate(createTopic3)
      val validatedCreateTopic4 = CreateTopicValidator.validate(createTopic4)

      validatedCreateTopic1 shouldBe Some(ApiError.emptyTopicField)
      validatedCreateTopic2 shouldBe Some(ApiError.emptyContentField)
      validatedCreateTopic3 shouldBe Some(ApiError.emptyUsernameField)
      validatedCreateTopic4 shouldBe Some(ApiError.wrongEmailFormat)
    }
  }

  "UpdatePostValidator" should {
    "return None if UpdateDiscussionTopic is valid" in {
      val updateValid = UpdatePost("test")

      val validatedUpdate = UpdatePostValidator.validate(updateValid)

      validatedUpdate shouldBe None
    }

    "return Some(ApiError) if CreateTopic is invalid" in {
      val updateInvalid = UpdatePost("")

      val validatedUpdatePost = UpdatePostValidator.validate(updateInvalid)

      validatedUpdatePost shouldBe Some(ApiError.emptyContentField)
    }
  }

  "CreatePostValidator" should {
    "return None if CreatePostValidator is valid" in {
      val createPostValid = CreatePost("test", "test", "test@wp.pl")

      val validatedCreatePost = CreatePostValidator.validate(createPostValid)

      validatedCreatePost shouldBe None
    }

    "return Some(ApiError) if CreateTopic is invalid" in {
      val createPostValid1 = CreatePost("", "test", "test")
      val createPostValid2 = CreatePost("test", "", "test")
      val createPostValid3 = CreatePost("test", "test", "")

      val validatedCreatePost1 = CreatePostValidator.validate(createPostValid1)
      val validatedCreatePost2 = CreatePostValidator.validate(createPostValid2)
      val validatedCreatePost3 = CreatePostValidator.validate(createPostValid3)

      validatedCreatePost1 shouldBe Some(ApiError.emptyContentField)
      validatedCreatePost2 shouldBe Some(ApiError.emptyUsernameField)
      validatedCreatePost3 shouldBe Some(ApiError.wrongEmailFormat)
    }
  }
}
