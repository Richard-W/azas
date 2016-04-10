package xyz.wiedenhoeft.azas.controllers

import spray.json._

sealed trait Validator {
  def validate(obj: JsValue): Boolean
}

object Validator {
  private val builtinValidators = Map[String, Validator] (
    "String" -> new Validator {
      override def validate(obj: JsValue): Boolean = obj match {
        case _: JsString ⇒ true
        case _           ⇒ false
      }
    },
    "Int" -> new Validator {
      override def validate(obj: JsValue): Boolean = obj match {
        case _: JsNumber ⇒ true
        case _           ⇒ false
      }
    },
    "Boolean" -> new Validator {
      override def validate(obj: JsValue): Boolean = obj match {
        case _: JsBoolean ⇒ true
        case _            ⇒ false
      }
    }
  )

  def get(ty: String): Validator = {
    if (builtinValidators.contains(ty)) builtinValidators(ty)
    else {
      val typeMap = Config.types.get(ty) match {
        case Some(s) ⇒ s
        case None    ⇒ throw new Exception("Unknown validation type: " + ty)
      }
      val neededValidators: Map[String, Validator] = (for (sub <- typeMap.values.toSeq.distinct) yield {
        (sub, Validator.get(sub))
      }).toMap
      val validationMap: Map[String, Validator] = (for (field <- typeMap.keys) yield {
        (field, neededValidators(typeMap(field)))
      }).toSeq.toMap
      new Validator {
        override def validate(v: JsValue): Boolean = v match {
          case obj: JsObject ⇒
            for (field <- obj.fields.keys) {
              validationMap.get(field) match {
                case Some(validator) ⇒
                  if (!validator.validate(obj.fields(field))) return false
                case None ⇒ return false
              }
            }
            return true
          case _ ⇒ false
        }
      }
    }
  }
}
