package com.djnz.fs2consumer

import cats.effect._
import com.djnz.avro
import fs2.kafka._
import fs2.kafka.vulcan.Auth
import fs2.kafka.vulcan.AvroSettings
import fs2.kafka.vulcan.SchemaRegistryClientSettings
import java.util.UUID

/** https://fd4s.github.io/fs2-kafka/docs/quick-example */
object ConsumerAvroStream extends App {

  val serverIp = "kafka.bla-bla-bla:9092"
  val consumerGroupId = "group-".concat(UUID.randomUUID().toString)
  val topicIn = "my-topic-in"

  /** one per SchemaRegistry */
  implicit val avroSettings: AvroSettings[IO] = AvroSettings {
    SchemaRegistryClientSettings[IO]("http://localhost:8081")
      .withAuth(Auth.Basic("username", "password"))
  }

  import avro.serdes._

  /** 3. consumer settings */
  val consumerSettings = ConsumerSettings[IO, String, avro.Order]
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withBootstrapServers(serverIp)
    .withGroupId(consumerGroupId)

  /** 4. stream of messages */
  val consumerSubscribed: fs2.Stream[IO, CommittableConsumerRecord[IO, String, avro.Order]] =
    KafkaConsumer
      .stream(consumerSettings)
      .evalTap(_.subscribeTo(topicIn))
      .flatMap(_.stream)

}
