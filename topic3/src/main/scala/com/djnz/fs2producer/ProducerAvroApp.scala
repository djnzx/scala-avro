package com.djnz.fs2producer

import cats.effect._
import cats.implicits.catsSyntaxOptionId
import com.djnz.avro
import fs2.kafka.vulcan.{Auth, AvroSettings, SchemaRegistryClientSettings}
import fs2.kafka._

import java.util.UUID

/** https://fd4s.github.io/fs2-kafka/docs/modules */
object ProducerAvroApp extends App {

  val serverIp = "kafka.bla-bla-bla:9092"
  val consumerGroupId = "ingest-".concat(UUID.randomUUID().toString)
  val topicOut = "my-topic-out"

  implicit val avroSettings: AvroSettings[IO] = AvroSettings {
    SchemaRegistryClientSettings[IO]("http://localhost:8081")
      .withAuth(Auth.Basic("username", "password"))
  }

  import avro.serdes._

  val producerSettings =
    ProducerSettings[IO, String, avro.Order]
      .withBootstrapServers(serverIp)

  val prod = KafkaProducer.resource(producerSettings)

  val item = avro.Order("qwer", 1.23.some, avro.Status.Paid(99.99))
  val record = ProducerRecord(topicOut, "ABC123", item)
  val records = ProducerRecords.one(record)

  val x: IO[ProducerResult[String, avro.Order]] = prod.use(_.produce(records).flatten)

}
