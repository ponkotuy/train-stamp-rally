package utils

import org.scalatest.FunSuite

class MissionTimeSuite extends FunSuite {
  test("MissionTime.addMinutes") {
    assert(MissionTime(1, 6, 1).addMinutes(15) === MissionTime(1, 6, 16))
    assert(MissionTime(1, 6, 23).addMinutes(-10) === MissionTime(1, 6, 13))
  }

  test("MissionTime.addMinutes if carry") {
    assert(MissionTime(1, 6, 59).addMinutes(3) === MissionTime(1, 7, 2))
    assert(MissionTime(1, 7, 3).addMinutes(-5) === MissionTime(1, 6, 58))
  }

  test("MissionTime.addMinutes if over hour") {
    assert(MissionTime(1, 23, 44).addMinutes(25) === MissionTime(2, 0, 9))
    assert(MissionTime(2, 0, 15).addMinutes(-18) === MissionTime(1, 23, 57))
  }

  test("MissionTime.fromString") {
    assert(MissionTime.fromString("1-06:15") === Some(MissionTime(1, 6, 15)))
  }
}
