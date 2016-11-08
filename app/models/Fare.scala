package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}

case class Fare(id: Long, companyId: Long, trainType: TrainType, km: Double, cost: Int) {
  def save()(implicit session: DBSession): Long = Fare.save(this)
}

object Fare extends SkinnyCRUDMapperWithId[Long, Fare] {
  override def defaultAlias: Alias[Fare] = createAlias("f")
  override def extract(rs: WrappedResultSet, n: ResultName[Fare]): Fare = autoConstruct(rs, n)

  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  def save(fare: Fare)(implicit session: DBSession): Long =
    createWithAttributes(
      'companyId -> fare.companyId,
      'trainType -> fare.trainType.value,
      'km -> fare.km,
      'cost -> fare.cost
    )

  def existsFare()(implicit session: DBSession): Seq[FareType] = withSQL {
    val f = defaultAlias
    select(sqls.distinct(f.companyId, f.trainType)).from(Fare as f)
  }.map(FareType.extract).list().apply()

  case class FareType(companyId: Long, trainType: TrainType)
  object FareType {
    def extract(rs: WrappedResultSet): FareType = {
      val f = defaultAlias
      FareType(rs.long(f.companyId), TrainType.find(rs.int(f.trainType)).getOrElse(TrainType.Local))
    }
  }
}
