package utils

import scala.concurrent.Future

object FutureUtil {
  def fromOption[A](opt: Option[A]): Future[A] =
    opt.fold(Future.failed[A](new RuntimeException("None")))(Future.successful)
}
