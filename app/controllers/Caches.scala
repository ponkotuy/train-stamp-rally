package controllers

import javax.inject.Inject

import authes.Authenticator
import authes.Role.Administrator
import caches.LineStationsCache
import play.api.mvc.InjectedController

class Caches @Inject() () extends InjectedController with Authenticator {
  import Responses._

  def clear() = Action { implicit req =>
    withAuth(Administrator) { _ =>
      LineStationsCache.clear()
      Success
    }
  }
}
