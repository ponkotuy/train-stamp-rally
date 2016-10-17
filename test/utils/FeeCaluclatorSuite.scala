package utils

import org.scalatest.FunSuite

class FeeCaluclatorSuite extends FunSuite {
  import FeeCalculator._
  test("300km normal fee") {
    assert(default(300) === default(301) - 10)
  }

  test("600km normal fee") {
    assert(default(600) === default(601) - 10)
  }
}
