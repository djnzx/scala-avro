package com.djnz.avro

import cats.effect.Resource
import cats.effect.Sync
import fs2.kafka.KeyDeserializer
import fs2.kafka.KeySerializer
import fs2.kafka.ValueDeserializer
import fs2.kafka.ValueSerializer
import fs2.kafka.vulcan.AvroDeserializer
import fs2.kafka.vulcan.AvroSerializer
import fs2.kafka.vulcan.AvroSettings

object serdes {

  implicit def mkKeyDeserializer[F[_]: Sync, A](implicit a: AvroDeserializer[A], as: AvroSettings[F]): Resource[F, KeyDeserializer[F, A]] =
    a.forKey(as)

  implicit def mkKeySerializer[F[_]: Sync, A](implicit a: AvroSerializer[A], as: AvroSettings[F]): Resource[F, KeySerializer[F, A]] =
    a.forKey(as)

  implicit def mkValueDeserializer[F[_]: Sync, A](implicit a: AvroDeserializer[A], as: AvroSettings[F]): Resource[F, ValueDeserializer[F, A]] =
    a.forValue(as)

  implicit def mkValueSerializer[F[_]: Sync, A](implicit a: AvroSerializer[A], as: AvroSettings[F]): Resource[F, ValueSerializer[F, A]] =
    a.forValue(as)

}
