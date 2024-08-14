package a7genrec

import a1schemas.Schemas
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class Sandbox extends AnyFunSuite with Matchers with ScalaCheckPropertyChecks {

  import a7genrec.syntax._

  val address = new GenericData.Record(Schemas.addressSchema)
  address.put("street", "Broadway")
  address.put("house", 123L)

  val p = new GenericData.Record(Schemas.sPerson3)
  p.put("name", "Jackery")
  p.put("age", 66)
  p.put("address", address)

  test("1. access valid fields") {
    val user1: GenericData.Record = new GenericData.Record(Schemas.sPerson1)
    user1.put("name", "Jim")
    user1.put("age", 33)

    val x1: Option[String] = user1.go[String]("name")
    val x2: Option[Int] = user1.go[Int]("age")
    pprint.log(x1)
    pprint.log(x2)
  }

  test("2. access valid fields") {
    val f1: Option[String] = p.go[String]("name")
    val f2: Option[GenericRecord] = p.go[GenericRecord]("address")
    val f3: Option[String] = p.go[String]("address.street")
    val f4: Option[Long] = p.go[Long]("address.house")

    pprint.log(f1)
    pprint.log(f2)
    pprint.log(f3)
    pprint.log(f4)
  }

  test("3. invalid field access - failure") {
    lazy val f5: Option[Long] = p.go[Long]("a") // Not a valid schema field: a
    lazy val f6: Option[Double] = p.go[Double]("address.street.x")
  }


}
