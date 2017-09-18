package authes

import models.Account
import play.api.mvc.{Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait Authenticator {
  import controllers.Responses._
  type User = Account

  val SessionCookie = "PLAY_SESSION"
  val TimeoutInSeconds = 7.days.toSeconds.toInt
  val container = new SessionContainer

  def findUser(token: String): Option[User] = {
    for {
      sessionId <- container.get(token)
      account <- Account.findById(sessionId)
    } yield account
  }

  def withAuth[A](role: Authority)(f: User => Result)(implicit req: Request[A]): Result = {
    val res = for {
      session <- req.session.get(SessionCookie).toRight(authorizationFailed)
      user <- findUser(session).toRight(authorizationFailed)
      res <- Either.cond(role.auth(user.role), f(user).withSession(SessionCookie -> session), authenticationFailed)
    } yield res
    res.merge
  }

  def withAuthAsync[A](role: Authority)(f: User => Future[Result])(implicit req: Request[A], ec: ExecutionContext): Future[Result] = {
    val res = for {
      session <- req.session.get(SessionCookie).toRight(suc(authorizationFailed))
      user <- findUser(session).toRight(suc(authorizationFailed))
      res <- Either.cond(role.auth(user.role), f(user).map(_.withSession(SessionCookie -> session)), suc(authenticationFailed))
    } yield res
    res.merge
  }

  def gotoLoginSucceeded(user: User) = {
    val token = container.startNewSession(user.id, TimeoutInSeconds)
    Success.withSession(SessionCookie -> token)
  }

  def gotoLogoutSucceeded[A]()(implicit req: Request[A]) = {
    req.session.get(SessionCookie).fold(authorizationFailed) { session =>
      container.remove(session)
      Success
    }
  }

  def authorizationFailed = Forbidden("Authorization failed")
  def authenticationFailed = Forbidden("Authentication failed")
  private def suc[A](x: A) = Future.successful(x)
}

trait Authority {
  def auth(userAuthority: Authority): Boolean
}
