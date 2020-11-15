package io.github.dserban.template

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.{Properties, Random}

object SessionKafkaProducer {
  def produce: Unit = {
    val kafkaBroker = "localhost"
    val kafkaPort = "9092"
    val kafkaTopic = "raw"
    val numberOfClicksPerButton = 20
    val waitTimeBetweenMessageBatch = 1700
    val chancesOfMissing = 8

    val properties = {
      val p = new Properties
      p.put("bootstrap.servers", s"$kafkaBroker:$kafkaPort")
      p.put("acks", "all")
      p.put("retries", "0")
      p.put("batch.size", "16384")
      p.put("linger.ms", "1")
      p.put("buffer.memory", "33554432")
      p.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      p
    }

    val producer = new KafkaProducer[String, String](properties)

    val r = new Random
    var sentCount = 0

    println(s"About to send to $kafkaTopic")
    (1 to numberOfClicksPerButton).foreach { click => {
      List("aaa", "bbb", "ccc").foreach { button => {
        if (r.nextInt(chancesOfMissing) != 0) {
          val message = s"""{"button": "$button", "tstamp": ${System.currentTimeMillis}}"""
          val producerRecord = new ProducerRecord[String,String](kafkaTopic, message)
          producer.send(producerRecord)
          sentCount += 1
        }
      } }
      println(s"Sent Count: $sentCount")
      Thread.sleep(waitTimeBetweenMessageBatch)
    } }

    producer.close
  }
}
