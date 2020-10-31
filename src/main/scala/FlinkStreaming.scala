package io.github.dserban.template

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.scala._
import java.util.Properties

object FlinkStreaming {
  def streamingDemo: Unit = {
    val kafkaBroker = "localhost"
    val kafkaPort = "9092"
    val kafkaTopic = "raw"
    val groupId = "demo"
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val properties = {
      val p = new Properties
      p.setProperty("bootstrap.servers", s"${kafkaBroker}:${kafkaPort}")
      p.setProperty("zookeeper.connect", "localhost:2181")
      p.setProperty("group.id", groupId)
      p
    }
    val wordCountStream: DataStream[String] =
      env.addSource(new FlinkKafkaConsumer(kafkaTopic, new SimpleStringSchema, properties))
    val wordsStream: DataStream[(String, Int)] =
      wordCountStream.flatMap(line => line.toUpperCase.split(" "))
                     .map(word => (word, 1))
    val countPair: DataStream[(String, Int)] =
      wordsStream.keyBy{ case (key,_) => key }.countWindow(5, 1).sum(1)
    countPair.print
    val executionResult: JobExecutionResult = env.execute("")
  }
}
