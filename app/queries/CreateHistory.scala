package queries

import models.History

case class CreateHistory(message: String) {
  def history() = History(0L, message, System.currentTimeMillis())
}
