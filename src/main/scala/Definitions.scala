package io.github.dserban.template

import upickle.default.{ReadWriter, macroRW}

case class Heartbeat(button: String, tstamp: Long)
object Heartbeat { implicit val rw: ReadWriter[Heartbeat] = macroRW }

case class SessionObj(startTime: Long, latestEndTime: Long, heartbeatCount: Int)
