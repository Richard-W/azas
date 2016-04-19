package xyz.wiedenhoeft.azas

import org.scalatest._
import spray.json._
import xyz.wiedenhoeft.azas.controllers.Validator

class ValidationSpec extends FlatSpec with Matchers {
  "Validators" must "validate correctly" in {
    val validator = Validator.get("A")

    val a1 = JsObject(
      "a" -> JsString("foo"),
      "b" -> JsObject(
        "b" -> JsString("bar")
      ),
      "num" -> JsNumber(1)
    )

    val a2 = JsObject(
      "c" -> JsString("foo"),
      "b" -> JsObject(
        "b" -> JsString("bar")
      ),
      "num" -> JsNumber(1)
    )

    val a3 = JsObject(
      "a" -> JsString("foo"),
      "b" -> JsObject(
        "b" -> JsString("bar")
      ),
      "num" -> JsNumber(3)
    )

    val obj3 = JsObject(
      "str" -> JsString("foo"),
      "e" -> JsString("1")
    )

    val obj4 = JsObject(
      "str" -> JsString("foo"),
      "e" -> JsString("3")
    )

    validator.validate(a1) should be (true)
    validator.validate(a2) should be (false)
    validator.validate(a3) should be (false)

    Validator.get("ContainsEnum").validate(obj3) should be (true)
    Validator.get("ContainsEnum").validate(obj4) should be (false)
  }
}
