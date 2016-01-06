package net.jetmq.broker

object MqttTopicClassificator {

  def checkTopicName(to: String): Boolean = {
    if (to != "#" && to.contains("#") &&
      (to.replace("#", "/#") != to.replace("/#", "//#") || to.last != '#')) {
      throw Bus.BadSubscriptionException(to)
    }

    if (to != "+" && to.contains("+") && (to.charAt(0) != '+') && to.replace("+", "/+") != to.replace("/+", "//+")) {
      throw Bus.BadSubscriptionException(to)
    }

    if (to.length > 0 && to.charAt(0) == '+')
      checkTopicName(to.substring(1))

    return true
  }

  def isSubclass(actual: String, subscribing: String): Boolean = {

    if (!subscribing.contains('#') && !subscribing.contains('+'))
      return isPlainSubclass(actual, subscribing)

    if (subscribing == "#")
      return true

    val square_index = subscribing.indexOf('#')

    if (square_index > 0) {
      val sub = if (square_index > 1) subscribing.substring(0, square_index - 1) + ".*" else "/.*"

      return isRegexSubclass(actual, sub)
    }

    return isRegexSubclass(actual, subscribing)
  }

  private def isPlainSubclass(actual: String, subscribing: String): Boolean =
    subscribing == actual

  private def isRegexSubclass(actual: String, subscribing: String): Boolean = {

    val reg = subscribing.zipWithIndex.map {
      case (c, i) => {
        if (c == '+')
          if (i == 0 || i == (subscribing.length - 1)) "[^/]*" else "[^/]+"
        else
          c.toString
      }
    }.mkString

    val res = actual.matches(reg)

    res
  }

  private def isSquaredSubclass(actual: String, sub: String) = {
    actual.startsWith(sub)
  }
}
