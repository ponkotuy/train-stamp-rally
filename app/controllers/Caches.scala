package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import caches.LineStationsCache
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import play.api.mvc.Controller

class Caches @Inject() () extends Controller with AuthElement with AuthConfigImpl {
  import Responses._

  def clear() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    LineStationsCache.clear()
    Success
  }
}
