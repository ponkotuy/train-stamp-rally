package utils

import models.{Company, Fare, TrainType}
import scalikejdbc._

object FeeCalculator {
  import models.DefaultAliases.f
  def calc(typ: TrainType, companyId: Long, distance: Double)(implicit session: DBSession): Option[Int] = {
    if (distance < 0.1) Some(0)
    else {
      val additional = if (typ == TrainType.Local) Some(0)
      else
        findFare(typ, companyId, Some(distance))
          .orElse(findFare(typ, companyId, None))
          .orElse(findFare(typ, Company.JR.id, Some(distance)))
          .orElse(findFare(typ, Company.JR.id, None))
      val base = findFare(TrainType.Local, companyId, Some(distance))
        .orElse(findFare(TrainType.Local, companyId, None))
        .orElse(findFare(TrainType.Local, Company.JR.id, Some(distance)))
        .orElse(findFare(TrainType.Local, Company.JR.id, None))
      for {
        b <- base
        a <- additional
      } yield b + a
    }
  }

  private[this] def findFare(typ: TrainType, companyId: Long, distance: Option[Double])(implicit session: DBSession): Option[Int] = {
    val where = sqls.eq(f.trainType, typ.value)
      .and.eq(f.companyId, companyId)
      .and(distance.map { d => sqls.ge(f.km, d) })
    val orderings = if (distance.isDefined) Seq(f.km) else Seq(f.km.desc)
    Fare.findAllByWithLimitOffset(where, limit = 1, orderings = orderings).headOption.map(_.cost)
  }

  class FeeCalculatorException extends Exception("JR fare not found.")
}
