package com.djnz.fs2producer

import cats.effect._
import fs2.Stream
import fs2.kafka._
import java.util.UUID

/** https://fd4s.github.io/fs2-kafka/docs/quick-example */
class ProducerApp[K: KeySerializer[IO, *], V: ValueSerializer[IO, *]] {

  val serverIp = "kafka.bla-bla-bla:9092"
  val consumerGroupId = "ingest-".concat(UUID.randomUUID().toString)
  val topicOut = "my-topic-out"

  val producerSettings =
    ProducerSettings[IO, K, V]
      .withBootstrapServers(serverIp)

  val producerStream: Stream[IO, KafkaProducer.PartitionsFor[IO, K, V]] =
    KafkaProducer.stream(producerSettings)

  def publish(k: K, v: V)(producer: KafkaProducer[IO, K, V]) = {
    val item: ProducerRecord[K, V] = ProducerRecord(topicOut, k, v)
    val records: ProducerRecords[K, V] = ProducerRecords.one(item)

    producer
      .produce(records)
      .flatten
  }

}

object ProducerApp extends App {
  new ProducerApp[String, String]
}
