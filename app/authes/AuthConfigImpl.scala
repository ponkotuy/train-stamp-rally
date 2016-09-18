package authes

import jp.t2v.lab.play2.auth._
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class AuthConfigImpl extends AuthConfig {
  override type Id = Long
  override type User = Account
  override type Authority = Role

  override val idTag: ClassTag[Id] = implicitly[ClassTag[Id]]

  override def sessionTimeoutInSeconds: Int = 3600

  override def resolveUser(id: Id)(implicit context: ExecutionContext): Future[Option[User]] =
    Future.successful(Account.findById(id))

  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Redirect(controllers.routes.Assets.at("game/index.html")))

  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Redirect(controllers.routes.Assets.at("index.html")))

  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Redirect(controllers.routes.Assets.at("index.html")))

  override def authorizationFailed(request: RequestHeader, user: User, authority: Option[Role])(implicit context: ExecutionContext): Future[Result] =
    Future.successful(Redirect(controllers.routes.Assets.at("index.html")))

  override def authorize(user: User, authority: Authority)(implicit context: ExecutionContext): Future[Boolean] = Future.successful{
    (user.role, authority) match {
      case (Role.Administrator, _) => true
      case (Role.NormalUser, Role.NormalUser) => true
      case _ => false
    }
  }
}
