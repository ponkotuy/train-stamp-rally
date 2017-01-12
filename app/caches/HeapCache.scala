package caches

trait HeapCache[T] {
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private[this] var cache: T = initializer()

  def initializer(): T

  def clear(): Unit = {
    cache = initializer()
  }

  def apply() = cache
}
