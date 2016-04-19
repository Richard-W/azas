package xyz.wiedenhoeft.azas.controllers

import spray.json._

/**
 * Validates JSON values against a scheme
 */
sealed trait Validator {
  def validate(obj: JsValue, options: Option[Seq[String]] = None): Boolean
}

object Validator {

  private val config = Config.scheme

  private val builtinValidators = Map[String, Validator] (
    "String" -> new Validator {
      override def validate(obj: JsValue, maybeOptions: Option[Seq[String]]): Boolean = obj match {
        case JsString(str) ⇒ maybeOptions match {
          case Some(options) ⇒ options.contains(str)
          case None          ⇒ true
        }
        case _ ⇒ false
      }
    },
    "Int" -> new Validator {
      override def validate(obj: JsValue, maybeOptions: Option[Seq[String]]): Boolean = obj match {
        case JsNumber(num) ⇒ maybeOptions match {
          case Some(options) ⇒ options.contains(num.toString)
          case None          ⇒ true
        }
        case _ ⇒ false
      }
    },
    "Boolean" -> new Validator {
      override def validate(obj: JsValue, maybeOptions: Option[Seq[String]]): Boolean = obj match {
        case _: JsBoolean ⇒ true
        case _            ⇒ false
      }
    }
  )

  /**
   * Initialize a validator for a given type that is specified in the configuration
   *
   * @param ty Name of the type
   * @return A validator for the type
   */
  def get(ty: String): Validator = {
    if (builtinValidators.contains(ty)) builtinValidators(ty)
    else if (config.types.contains(ty)) {
      val fields = config.types(ty)
      val neededValidators: Map[String, Validator] = ((fields map { field ⇒ field.ty }).distinct map { ty ⇒
        (ty, get(ty))
      }).toMap
      val validationMap: Map[String, (Validator, Option[Seq[String]])] = (fields map { field ⇒
        (field.field, (neededValidators(field.ty), field.options))
      }).toMap
      new Validator {
        override def validate(v: JsValue, options: Option[Seq[String]]): Boolean = v match {
          case obj: JsObject ⇒
            validationMap foreach {
              case (name, (validator, maybeOptions)) ⇒
                obj.fields.get(name) match {
                  case Some(value) ⇒
                    if (!validator.validate(value, maybeOptions)) return false
                  case None ⇒
                    return false
                }
            }
            obj.fields.size == validationMap.size
          case _ ⇒ false
        }
      }
    } else {
      throw new Exception("Unknown type/enum: " + ty)
    }
  }
}
