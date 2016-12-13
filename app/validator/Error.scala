package validator

import org.json4s.CustomSerializer
import org.json4s.JsonDSL._

abstract class Error {
  def message: String
  def url: Option[String]
}

object ErrorSerializer extends CustomSerializer[Error](format => (
  { PartialFunction.empty },
  {
    case x: Error =>
      ("message" -> x.message) ~ ("url" -> x.url)
  }
))
