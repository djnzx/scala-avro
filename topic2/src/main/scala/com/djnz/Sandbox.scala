package com.djnz

import cats.implicits._
import shapeless.{:+:, CNil, Coproduct}

object Sandbox extends App {

  type T = avro.Status1 :+: avro.Status2 :+: avro.Status3 :+: CNil

  val s1 = avro.Status1("whatever")

  val s1lifted: T = Coproduct[T](s1)

  val u = avro.User("Jim", 33, s1lifted.some)

  pprint.log(u)

}
