package utils

import scala.collection.GenIterable

object Zip {
  def zip3[A, B, C](
    as: GenIterable[A], bs: GenIterable[B], cs: GenIterable[C]
  ): List[(A, B, C)] = {
    def main(
      a: GenIterable[A], b: GenIterable[B], c: GenIterable[C]
    ): List[(A, B, C)] = {
      if(a.nonEmpty && b.nonEmpty && c.nonEmpty) {
        (a.head, b.head, c.head) ::
          main(a.tail, b.tail, c.tail)
      } else {
        Nil
      }
    }
    main(as, bs, cs)
  }

  def zip6[A, B, C, D, E, F](
    as: GenIterable[A], bs: GenIterable[B], cs: GenIterable[C],
    ds: GenIterable[D], es: GenIterable[E], fs: GenIterable[F]
  ): List[(A, B, C, D, E, F)] = {
    def main(
      a: GenIterable[A], b: GenIterable[B], c: GenIterable[C],
      d: GenIterable[D], e: GenIterable[E], f: GenIterable[F]
    ): List[(A, B, C, D, E, F)] = {
      if(a.nonEmpty && b.nonEmpty && c.nonEmpty && d.nonEmpty && e.nonEmpty && f.nonEmpty) {
        (a.head, b.head, c.head, d.head, e.head, f.head) ::
          main(a.tail, b.tail, c.tail, d.tail, e.tail, f.tail)
      } else {
        Nil
      }
    }
    main(as, bs, cs, ds, es, fs)
  }
}
