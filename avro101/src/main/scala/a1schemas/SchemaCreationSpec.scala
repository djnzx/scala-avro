package a1schemas

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import a9tools.Utils.contentsFromResources

class SchemaCreationSpec extends AnyFunSuite with Matchers with ScalaCheckPropertyChecks {

  test("1. create schema programmatically") {
      // format: off
      val s: Schema = SchemaBuilder.record("person").fields()
        .name("name").`type`.stringType.noDefault
        .name("age").`type`.nullable.intType.noDefault
        .endRecord
      // format: on
    pprint.log(s)
  }

  test("2. create schema programmatically, nested") {
      // format: off
      val sAddr = SchemaBuilder.record("Address").fields()
        .name("street").`type`.stringType.noDefault
        .name("house").`type`.longType().noDefault
        .endRecord()

      val sPerson = SchemaBuilder.record("Person").fields()
        .name("name").`type`.stringType.noDefault
        .name("age").`type`.nullable.intType.noDefault
        .name("address").`type`(sAddr).noDefault
        .endRecord
      // format: on

    pprint.log(sPerson)
  }

  test("3. read and parse schema from file") {
    val contents: String = contentsFromResources("1/schema3.json")
    pprint.log(contents)

    val s: Schema = new Schema.Parser().parse(contents) // SchemaParseException
    pprint.log(s)
  }

}
