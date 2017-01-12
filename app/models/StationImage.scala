package models

import scalikejdbc._
import skinny.orm.{Alias, SkinnyNoIdCRUDMapper}

final case class StationImage(id: Long, imageId: Option[Long], image: Option[Image] = None) {
  def save()(implicit session: DBSession): Unit = StationImage.save(this)
}

object StationImage extends SkinnyNoIdCRUDMapper[StationImage] {
  override val defaultAlias: Alias[StationImage] = createAlias("si")

  override def extract(rs: WrappedResultSet, n: ResultName[StationImage]): StationImage = autoConstruct(rs, n, "image")

  belongsToWithJoinCondition[Image](
    right = Image,
    on = sqls.eq(defaultAlias.imageId, Image.defaultAlias.id),
    merge = (station, image) => station.copy(image = image)
  ).byDefault

  def findById(id: Long)(implicit session: DBSession = autoSession) =
    findBy(sqls.eq(defaultAlias.id, id))

  def save(img: StationImage)(implicit session: DBSession): Unit =
    createWithAttributes('id -> img.id, 'imageId -> img.imageId)
}
