package authes

import java.security.SecureRandom

import models.Session
import scalikejdbc._

import scala.annotation.tailrec
import scala.util.Random

class SessionContainer {
  import SessionContainer._

  type AuthenticityToken = String

  val random = new Random(new SecureRandom())

  def startNewSession(userId: Long, timeoutInSeconds: Int): AuthenticityToken = {
    val now = System.currentTimeMillis()
    val token = generate
    Session(0L, userId, token, now, now + timeoutInSeconds.toLong).save()(AutoSession)
    token
  }

  def remove(token: AuthenticityToken): Unit =
    Session.deleteBy(sqls.eq(Session.column.token, token))

  def get(token: AuthenticityToken): Option[Long] =
    Session.findByToken(token).map(_.accountId)

  def prolongTimeout(token: AuthenticityToken, timeoutInSeconds: Int): Unit =
    Session.findByToken(token).map { s =>
      s.copy(expire = System.currentTimeMillis() + timeoutInSeconds).save()
    }

  @tailrec
  private final def generate: AuthenticityToken = {
    val table = "abcdefghijklmnopqrstuvwxyz1234567890_.~*'()"
    val token = Iterator.continually(random.nextInt(table.length)).map(table).take(TokenSize).mkString
    if (get(token).isDefined) generate else token
  }

}

object SessionContainer {
  val TokenSize = 64
  implicit val db: DBSession = AutoSession
}
