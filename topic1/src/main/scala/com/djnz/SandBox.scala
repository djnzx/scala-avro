package com.djnz

object SandBox extends App {

  /** all of them extend `avro.specific.SpecificRecord`
    * the main drawback is Object as a type when we use:
    * - NULLABLE parameter
    * - UNION type
    */
  val u1 = new avro.User("Jim", 33, null)
  val u2 = new avro.User("Jim", 33, new avro.Status1("whatever"))
  val u3 = new avro.User("Jim", 33, new avro.Status2(42))
  val u4 = new avro.User("Jim", 33, new avro.Status3(1L))

  pprint.log(u1)
  pprint.log(u2)
  pprint.log(u3)
  pprint.log(u4)
}
