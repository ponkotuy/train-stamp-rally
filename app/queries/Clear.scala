package queries

case class Clear(rate: Int) {
  def isValid: Boolean = -3 <= rate && rate <= 3
}
