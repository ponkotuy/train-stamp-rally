package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyNoIdCRUDMapper}
import utils.ImgAttr

import scala.xml.Node

case class ImageAttribute(
    id: Long,
    fileName: String,
    name: String,
    artist: Node,
    licenseShortName: String,
    licenseUrl: String,
    credit: Node,
    created: Long
) extends Attr {
  def save()(implicit session: DBSession): Unit = ImageAttribute.save(this)
}

object ImageAttribute extends SkinnyNoIdCRUDMapper[ImageAttribute] {
  override val defaultAlias: Alias[ImageAttribute] = createAlias("ia")

  override def extract(rs: WrappedResultSet, n: ResultName[ImageAttribute]): ImageAttribute = new ImageAttribute(
    id = rs.get[Long](n.id),
    fileName = rs.get[String](n.fileName),
    name = rs.get[String](n.name),
    artist = ImgAttr.toXML(rs.get[String](n.artist)).getOrElse(<p></p>),
    licenseShortName = rs.get[String](n.licenseShortName),
    licenseUrl = rs.get[String](n.licenseUrl),
    credit = ImgAttr.toXML(rs.get[String](n.credit)).getOrElse(<p></p>),
    created = rs.get[Long](n.created)
  )

  def findById(id: Long)(implicit session: DBSession = autoSession) =
    findBy(sqls.eq(defaultAlias.id, id))

  def save(attr: ImageAttribute)(implicit session: DBSession): Unit =
    createWithAttributes(
      'id -> attr.id,
      'fileName -> attr.fileName,
      'name -> attr.name,
      'artist -> attr.artist.toString(),
      'licenseShortName -> attr.licenseShortName,
      'licenseUrl -> attr.licenseUrl,
      'credit -> attr.credit.toString(),
      'created -> attr.created
    )
}

trait Attr {
  def fileName: String
  def name: String
  def artist: Node
  def licenseShortName: String
  def licenseUrl: String
  def credit: Node

  def attribution = <span>By { artist } ({ credit }) [<a href={ licenseUrl } target="_blank">{ licenseShortName }</a>], <a href={ url } target="_blank">via Wikimedia Commons</a></span>
  def url = s"https://commons.wikimedia.org/wiki/File:${fileName}"
}
