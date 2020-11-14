package io.github.dserban.template

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector
import org.apache.flink.api.scala._
import java.util.Properties

object FlinkHeartbeat {
  def heartbeatDemo: Unit = {
    val kafkaBroker = "localhost"
    val kafkaPort = "9092"
    val kafkaTopic = "raw"
    val groupId = "demo"
    val sessionTimeOut = 1000
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val properties = {
      val p = new Properties
      p.setProperty("bootstrap.servers", s"${kafkaBroker}:${kafkaPort}")
      p.setProperty("zookeeper.connect", "localhost:2181")
      p.setProperty("group.id", groupId)
      p
    }

    val plainMessageStream: DataStream[String] =
      env.addSource(new FlinkKafkaConsumer(kafkaTopic, new SimpleStringSchema, properties))

    val processFunction = new ProcessFunction[(String,Long), SessionObj] {
      private var state: ValueState[SessionObj] = null

      override def open(parameters: Configuration): Unit = {
        state = getRuntimeContext.getState(new ValueStateDescriptor[SessionObj]("flinkState", classOf[SessionObj]))
      }

      override def processElement(value: (String, Long),
                                  ctx: ProcessFunction[(String, Long), SessionObj]#Context,
                                  out: Collector[SessionObj]): Unit = {
        val currentSession = state.value
        var outBoundSessionRecord: SessionObj = null
        if (currentSession == null) {
          outBoundSessionRecord = SessionObj(value._2, value._2, 1)
        } else {
          outBoundSessionRecord = SessionObj(currentSession.startTime, value._2, currentSession.heartbeatCount + 1)
        }
        state.update(outBoundSessionRecord)
        out.collect(outBoundSessionRecord)
        ctx.timerService.registerEventTimeTimer(System.currentTimeMillis + sessionTimeOut)
      }

      override def onTimer(timestamp: Long,
                           ctx: ProcessFunction[(String, Long), SessionObj]#OnTimerContext,
                           out: Collector[SessionObj]): Unit = {
        val result = state.value
        if (result != null && result.latestEndTime + sessionTimeOut < System.currentTimeMillis) {
          state.clear
        }
      }
    }

    val heartbeatMessageStream: DataStream[SessionObj] =
      plainMessageStream.map { plainMessage => {
        println(s"Plain Message: $plainMessage")
        val hb = upickle.default.read[Heartbeat](plainMessage)
        (hb.button, hb.tstamp)
      }}.keyBy(_._1).process(processFunction)

    heartbeatMessageStream.map { session => {
      println(s"Session: $session")
    }}

    heartbeatMessageStream.print

    val executionResult: JobExecutionResult = env.execute("")
  }
}
