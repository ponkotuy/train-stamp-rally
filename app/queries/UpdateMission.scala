package queries

case class UpdateMission(introduction: String, clearText: String) {
  def attributes = Seq(
    'introduction -> introduction,
    'clearText -> clearText
  )
}
