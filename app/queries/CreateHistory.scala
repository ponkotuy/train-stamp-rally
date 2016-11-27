package queries

import models.History

case class CreateHistory(message: String, release: Option[Long]) {
  def history() = History(0L, message, System.currentTimeMillis())
}
