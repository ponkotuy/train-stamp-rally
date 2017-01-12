package scrape.model

import scala.collection.GenIterable

final case class Train(
  name: String,
  number: String,
  typ: String,
  stops: GenIterable[Stop],
  url: String,
  special: Boolean
)
