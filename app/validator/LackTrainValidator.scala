package validator

class LackTrainValidator(diagrams: Set[Long]) {
  def validate(diagramId: Long): Option[Error] =
    if (diagrams.contains(diagramId)) None else Some(new LackTrainsError(diagramId))
}

class LackTrainsError(diagramId: Long) extends Error {
  override def message: String = s"DiagramId = ${diagramId}: Lack of trains"

  override def url: Option[String] = Some(s"/creator/diagram/index.html?edit=${diagramId}")
}
