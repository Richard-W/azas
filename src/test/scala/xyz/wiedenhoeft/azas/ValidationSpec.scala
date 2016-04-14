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

    val obj3 = JsObject(
      "str" -> JsString("foo"),
      "e" -> JsString("1")
    )

    val obj4 = JsObject(
      "str" -> JsString("foo"),
      "e" -> JsString("3")
    )

    validator.validate(obj1) should be (true)
    validator.validate(obj2) should be (false)

    Validator.get("ContainsEnum").validate(obj3) should be (true)
    Validator.get("ContainsEnum").validate(obj4) should be (false)
  }
}
