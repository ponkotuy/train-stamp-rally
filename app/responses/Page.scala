package responses

case class WithPage[T](pagination: Page, data: T)
case class Page(total: Long, size: Int, current: Int, last: Int)
