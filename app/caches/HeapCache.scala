package caches

trait HeapCache[T] {
  private[this] var cache: T = initializer()

  def initializer(): T

  def clear(): Unit = {
    cache = initializer()
  }

  def apply() = cache
}
