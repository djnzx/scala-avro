package com.djnz

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

package object avro {

  /** model */
  sealed trait Status
  object Status {
    case class Placed(date: LocalDateTime) extends Status
    case class Paid(amount: Double) extends Status
    case class Shipped(by: String) extends Status
    case class Delivered(at: Long) extends Status
  }
  case class Order(number: String, amount: Option[Double], status: Status)

  /** 1. derive avro schema (automatically) */
  import _root_.vulcan.Codec
  import _root_.vulcan.generic._
  implicit val cLdt: Codec[LocalDateTime] = Codec[Instant]
    .imap { instant =>
      LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
    } { ldt =>
      ldt.toInstant(ZoneOffset.UTC)
    }
  implicit val cStatus: Codec[Status] = Codec.derive[Status]
  implicit val cOrder: Codec[Order] = Codec.derive[Order]

  /** 2. derive Kafka Avro ValueDeserializer */
  import fs2.kafka.vulcan._
  implicit val orderAvroDeserializer: AvroDeserializer[avro.Order] =
    avroDeserializer[avro.Order]
  implicit val orderAvroSerializer: AvroSerializer[avro.Order] =
    avroSerializer[avro.Order]

}
