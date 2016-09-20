package responses

import models.{Line, LineStation, Station, TrainType}
import utils.TrainTime

case class TrainResponse(
    id: Long,
    start: TrainTime,
    diagramId: Long,
    name: String,
    trainType: TrainType,
    subType: String,
    stops: Seq[TrainStop]
)

case class TrainStop(
    id: Long,
    arrival: TrainTime,
    departure: TrainTime,
    lineStation: LineStation,
    line: Line,
    station: Station
)
