package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.Company
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller

class Companies @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._
  implicit val formats = DefaultFormats

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    Ok(Extraction.decompose(Company.findAll(Seq(Company.defaultAlias.id))))
  }
}
