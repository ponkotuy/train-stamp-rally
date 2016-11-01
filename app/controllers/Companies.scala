package controllers

import authes.AuthConfigImpl
import authes.Role.Administrator
import com.github.tototoshi.play2.json4s.Json4s
import com.google.inject.Inject
import jp.t2v.lab.play2.auth.AuthElement
import models.Company
import org.json4s.{DefaultFormats, Extraction}
import play.api.mvc.Controller
import utils.GoogleSheets

import scala.collection.JavaConverters._

class Companies @Inject()(json4s: Json4s) extends Controller with AuthElement with AuthConfigImpl {
  import json4s._
  implicit val formats = DefaultFormats

  def list() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    Ok(Extraction.decompose(Company.findAll(Seq(Company.defaultAlias.id))))
  }

  def load() = StackAction(AuthorityKey -> Administrator) { implicit req =>
    val sheetId = "1Vf19gIYCDgeoB5FYCi3PiqFJ69EEBnVXO6WFfWRyVlA"
    val service = GoogleSheets.getSheetServices
    val response = service.spreadsheets().values().get(sheetId, "A1:B3")
    Ok(Extraction.decompose(response.values().asScala))
  }
}
