package utils

import scala.collection.{breakOut, mutable}
import scala.util.Random

trait MethodProfiler {
  def apply[T](name: String)(f: => T): T
  def clear(): Unit
  def log(): Unit
  def result: Map[String, Long]
}

object MethodProfiler {
  def apply(): MethodProfiler = new ProfilerImpl()
  /**
   * 確率で実際にProfile取るInstanceを返す。
   *
   * @param rate: rate分の1の確率でImplを返す。残りはdummyで何もしないclassを返す。
   */
  def genRandom(rate: Int): MethodProfiler = {
    if (Random.nextInt(rate) == 0) new ProfilerImpl()
    else NopProfiler
  }

  case class Start(name: String, time: Long) {
    def diff(): Long = System.nanoTime() - time
  }

  def Nop = NopProfiler
}

class ProfilerImpl() extends MethodProfiler {
  import MethodProfiler._

  var starts: List[Start] = Nil
  val results: mutable.Map[String, Long] = mutable.LinkedHashMap[String, Long]().withDefaultValue(0L)

  override def apply[T](name: String)(f: => T): T = {
    synchronized {
      val names = starts.map(_.name).lastOption.fold(name) { it => s"${it}.${name}" }
      starts = Start(names, System.nanoTime()) :: starts
    }
    try { f } finally {
      end()
      if (starts.isEmpty) log()
    }
  }

  def log(): Unit = synchronized {
    val prettyResults: List[String] = results.map { case (k, v) => s"${k}: ${v / 1000000.0}ms" }(breakOut)
    println(("MethodProfiler result" :: prettyResults).mkString("\n"))
  }

  private def end(): Unit = synchronized {
    val st :: rest = starts
    starts = rest
    results(st.name) += st.diff()
  }

  def result: Map[String, Long] = synchronized {
    results.map(identity)(breakOut)
  }

  def clear(): Unit = {
    starts = Nil
    results.clear()
  }

  override def toString: String = s"ProfilerImpl\nstarts:${starts.mkString(",")}\nresults:${results.mkString(",")}"
}

object NopProfiler extends MethodProfiler {
  override def apply[T](name: String)(f: => T): T = f
  override def clear(): Unit = {}
  override def log(): Unit = {}
  override def result: Map[String, Long] = Map.empty
}
