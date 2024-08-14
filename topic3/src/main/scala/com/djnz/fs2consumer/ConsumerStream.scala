package com.djnz.fs2consumer

import cats.effect._
import fs2.kafka._
import java.util.UUID

/** https://fd4s.github.io/fs2-kafka/docs/quick-example */
object ConsumerStream extends App {

  val serverIp = "kafka.bla-bla-bla:9092"
  val consumerGroupId = "group-".concat(UUID.randomUUID().toString)
  val topicIn = "my-topic-in"

  /** consumer consumes Array[Byte]
    * so, out of the box, provides following deserialization instances:
    * - long
    * - int
    * - short
    * - uuid
    * - double
    * - float
    * - unit
    * - sting
    */
  val consumerSettings = ConsumerSettings[IO, String, String]
    .withAutoOffsetReset(AutoOffsetReset.Earliest)
    .withBootstrapServers(serverIp)
    .withGroupId(consumerGroupId)

  val consumerSubscribed: fs2.Stream[IO, CommittableConsumerRecord[IO, String, String]] =
    KafkaConsumer
      .stream[IO, String, String](consumerSettings)
      .evalTap(_.subscribeTo(topicIn))
      .flatMap(_.stream)

}
