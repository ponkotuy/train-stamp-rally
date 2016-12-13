package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import utils.MissionTime

import scala.collection.breakOut

case class Score(
    id: Long,
    missionId: Long,
    accountId: Long,
    time: MissionTime,
    distance: Double,
    money: Int,
    rate: Int,
    created: Long,
    account: Option[Account] = None
) {
  def save()(implicit session: DBSession): Long = Score.save(this)
}

object Score extends SkinnyCRUDMapperWithId[Long, Score] {
  override def idToRawValue(id: Long): Any = id
  override def rawValueToId(value: Any): Long = value.toString.toLong

  override val defaultAlias: Alias[Score] = createAlias("sc")

  override def extract(rs: WrappedResultSet, n: ResultName[Score]): Score = autoConstruct(rs, n, "account")

  lazy val accountRef = belongsTo[Account](
    right = Account,
    merge = (s, a) => s.copy(account = a)
  )

  def ranking(column: SQLSyntax, where: SQLSyntax, limit: Int)(implicit session: DBSession): Seq[Score] = {
    val result = withSQL {
      select.from(Score as defaultAlias).append(
        sqls"""inner join (select account_id, MIN(${column}) as max_value from score where ${where} group by account_id) as sc2
          on sc.account_id = sc2.account_id and sc.${column} = sc2.max_value
          group by sc.account_id order by ${column} limit ${limit}"""
      )
    }.map { rs => extract(rs, defaultAlias.resultName) }.toList().apply()
    val accounts: Map[Long, Account] =
      Account.findAllByIds(result.map(_.accountId): _*).map { a => a.id -> a }(breakOut)
    result.map { r => r.copy(account = accounts.get(r.accountId)) }
  }

  def save(score: Score)(implicit session: DBSession): Long = {
    MissionRate.upsert(score.missionId, score.rate)
    createWithAttributes(
      'missionId -> score.missionId,
      'accountID -> score.accountId,
      'time -> score.time.toString,
      'money -> score.money,
      'distance -> score.distance,
      'rate -> score.rate,
      'created -> score.created
    )
  }
}
