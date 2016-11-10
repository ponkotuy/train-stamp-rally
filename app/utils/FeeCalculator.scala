package utils

import models.{Company, Fare, TrainType}
import scalikejdbc._

object FeeCalculator {
  import models.DefaultAliases.f
  def calc(typ: TrainType, company: Company, distance: Double)(implicit session: DBSession): Int = {
    val additional = if(typ == TrainType.Local) 0
    else
      findFare(typ, company, Some(distance))
          .orElse(findFare(typ, company, None))
          .orElse(findFare(typ, Company.JR, Some(distance)))
          .orElse(findFare(typ, Company.JR, None))
          .getOrElse(throw new FeeCalculatorException)
    val base = findFare(TrainType.Local, company, Some(distance))
        .orElse(findFare(TrainType.Local, company, None))
        .orElse(findFare(TrainType.Local, Company.JR, Some(distance)))
        .orElse(findFare(TrainType.Local, Company.JR, None))
        .getOrElse(throw new FeeCalculatorException)
    base + additional
  }

  private[this] def findFare(typ: TrainType, company: Company, distance: Option[Double])(implicit session: DBSession): Option[Int] = {
    val where = sqls.eq(f.trainType, typ.value)
        .and.eq(f.companyId, company.id)
        .and(distance.map { d => sqls.ge(f.km, d) })
    val orderings = if(distance.isDefined) Seq(f.km) else Seq(f.km.desc)
    Fare.findAllByWithLimitOffset(where, limit = 1, orderings = orderings).headOption.map(_.cost)
  }

  class FeeCalculatorException extends Exception("JR fare not found.")
}
