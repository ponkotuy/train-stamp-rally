package utils

import java.text.Normalizer

import scala.collection.breakOut

object ParseHelper {
  def norm(str: String) = Normalizer.normalize(str, Normalizer.Form.NFKC).trim
  def normList(sep: Char)(str: String): List[String] = str.trim.split(sep).map(_.trim)(breakOut)
}
