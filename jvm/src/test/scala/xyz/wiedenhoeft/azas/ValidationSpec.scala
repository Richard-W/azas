package xyz.wiedenhoeft.azas

import org.scalatest._
import spray.json._
import xyz.wiedenhoeft.azas.controllers.Validator

class ValidationSpec extends FlatSpec with Matchers {
  "Validators" must "validate correctly" in {
    val validator = Validator.get("A")

    val obj1 = JsObject(
      "a" -> JsString("foo"),
      "b" -> JsObject(
        "b" -> JsString("bar")
      )
    )

    val obj2 = JsObject(
      "c" -> JsString("foo"),
      "b" -> JsObject(
        "b" -> JsString("bar")
      )
    )

    validator.validate(obj1) should be (true)
    validator.validate(obj2) should be (false)
  }
}
