package controllers

import play.api.mvc.{Action, Controller}

class MyAssets extends Controller {
  def at(path: String, file: String, aggressiveCaching: Boolean = false) = {
    println(path, file)
    Assets.at(path, file, aggressiveCaching)
  }
}
