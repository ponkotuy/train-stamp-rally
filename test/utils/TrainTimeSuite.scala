package utils

import org.scalatest.FunSuite

class TrainTimeSuite extends FunSuite {
  test("TrainTime.addMinutes") {
    assert(TrainTime(6, 1).addMinutes(15) === TrainTime(6, 16))
    assert(TrainTime(6, 23).addMinutes(-10) === TrainTime(6, 13))
  }

  test("TrainTime.addMinutes if carry") {
    assert(TrainTime(6, 59).addMinutes(3) === TrainTime(7, 2))
    assert(TrainTime(7, 3).addMinutes(-5) === TrainTime(6, 58))
  }

  test("TrainTime.addMinutes if over hour") {
    assert(TrainTime(23, 44).addMinutes(25) === TrainTime(0, 9))
    assert(TrainTime(0, 15).addMinutes(-18) === TrainTime(23, 57))
  }
}
