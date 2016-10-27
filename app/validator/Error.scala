package validator

import org.json4s.JsonDSL._
import org.json4s.{CustomSerializer, JValue}

abstract class Error {
  def message: String
}

object ErrorSerializer extends CustomSerializer[Error](format => (
    {PartialFunction.empty},
    {
      case x: Error =>
        "message" -> x.message: JValue
    }
))
