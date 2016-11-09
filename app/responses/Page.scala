package responses

case class WithPage[T](pagination: Page, data: T)
case class Page(total: Long, size: Int, current: Int, last: Int)

object Page {
  def apply(total: Long, size: Int, current: Int) = new Page(
    total = total,
    size = size,
    current = current,
    last = ((total + size - 1) / size).toInt
  )
}
