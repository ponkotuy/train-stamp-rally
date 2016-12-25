package queries

import play.api.data.Form
import play.api.data.Forms._
import skinny.Pagination

case class Paging(page: Int, size: Int, q: Option[String]) {
  def pagination = Pagination.page(page).per(size)
  def from = (page - 1) * size
  def to = page * size
}

object Paging {
  val form = Form(
    mapping(
      "page" -> number(min = 1),
      "size" -> number(min = 2),
      "q" -> optional(text)
    )(Paging.apply)(Paging.unapply)
  )
}
