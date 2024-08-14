package a2data

import a1schemas.Schemas
import a9tools.Console.delimiter
import a9tools.Tools.readFrom
import cats.implicits.catsSyntaxEitherId
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import org.apache.avro.Schema
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.DirectBinaryEncoder
import org.apache.avro.io.EncoderFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scala.util.Try

object DataRelated {

  /** record create */
  val user1: GenericData.Record = new GenericData.Record(Schemas.sPerson1)
  user1.put("name", "Jim")
  user1.put("age", 33)
//  user1.put("age", true) // will not fail !!!

  val user2: GenericData.Record = new GenericData.Record(Schemas.sPerson1)
  user2.put("name", "Jack")
  user2.put("age", 44)

  val user3: GenericData.Record = new GenericData.Record(Schemas.sPerson1)
  user3.put("name", "Alex")

  val user4: GenericData.Record = new GenericData.Record(Schemas.sPerson1)
  user4.put("age", 55)

  val user5: GenericData.Record = new GenericData.Record(Schemas.sPerson1)

  /** generic data fails only on absent attribute names */
//  user1.put("whatever", new Object)

}

class DataRelated extends AnyFunSuite with Matchers with ScalaCheckPropertyChecks {

  import DataRelated._

  val f = new File("persons.bin")

  test("1. GenericRecord => byte[]") {
    // output stream
    val os = new ByteArrayOutputStream()
    // encoder attached to output stream
    val enc = EncoderFactory.get.binaryEncoder(os, null)
    // writer aware of schema
    val dw = new GenericDatumWriter[GenericData.Record](Schemas.sPerson1)
    // write
    dw.write(user1, enc)

    // flush encoder
    enc.flush()
    // close stream
    os.close()

    // original data
    pprint.log(user1)
    // {"name": "Jim", "age": 33}

    // encoded bytes
    val bytes = os.toByteArray
    pprint.log(bytes)
    // Array(6, 74, 105, 109, 0, 66)
    pprint.log(new String(bytes))
    //   marker 'J' 'i'  'm' end  ?
    //    \u0006 Jim        \u0000B
  }

  test("1. byte[] => write GenericRecord") {
    // serialized input
    val in: Array[Byte] = Array(6, 74, 105, 109, 0, 66)
    // decoder attached to RAW bytes
    val dec = DecoderFactory.get.binaryDecoder(in, null)
    // reader aware of schema
    val dr = new GenericDatumReader[GenericData.Record](Schemas.sPerson1)
    // read
    val readed: GenericData.Record = dr.read(user1, dec)
    pprint.log(readed)
  }

  test("2. write one item to a file with schema attached") {

    /** 1. writer which is aware of schema */
    val gw = new GenericDatumWriter[GenericRecord](Schemas.sPerson1)

    /** 2. writer */
    val w = new DataFileWriter[GenericRecord](gw)

    /** 3. create file */
    w.create(Schemas.sPerson1, f)

    /** 4. write to the file */
    w.append(user1)

    /** close the file */
    w.close()
  }

  test("2. write to a file") {

    def writeTo(file: File, schema: Schema, items: GenericRecord*) = {
      val gw = new GenericDatumWriter[GenericRecord](schema)
      val w = new DataFileWriter[GenericRecord](gw)
      w.create(schema, file)
      items.foreach(w.append)
      w.close()
    }

    writeTo(f, Schemas.sPerson1, user1, user2, user3)
  }

  test("3a. read - no schema enforced") {
    readFrom(f)
      .foreach { x =>
        pprint.log(x.getSchema)
        pprint.log(x)
      }
  }

  test("3b. read - WITH schema enforced") {
    readFrom(f, Schemas.sPerson1)
      .foreach { x =>
        pprint.log(x.getSchema)
        pprint.log(x)
      }
  }

  test("4. read - WITH WRONG schema enforced (missing field)") {
    an[org.apache.avro.AvroTypeException] shouldBe thrownBy {
      readFrom(f, Schemas.sPerson2)
        .foreach { x =>
          pprint.log(x.getSchema)
          pprint.log(x)
        }
    }
  }

  test("5. validation - just writing to the memory") {

    def validateRecord(record: GenericRecord) = {
      val schema = record.getSchema
      val gw = new GenericDatumWriter[GenericRecord](schema)
      val w = new DataFileWriter[GenericRecord](gw)
      val os = new ByteArrayOutputStream()
      w.create(schema, os)
      val outcome = Try(w.append(record)).fold(_.getCause.asLeft, _ => record.asRight)
      w.close()
      os.close()
      outcome
    }

    pprint.log(user1) // {"name": "Jim", "age": 33}
    pprint.log(validateRecord(user1))
    pprint.log(user2) // {"name": "Jack", "age": 44}
    pprint.log(validateRecord(user2))
    pprint.log(user3) // {"name": "Alex", "age": null}
    pprint.log(validateRecord(user3))
    pprint.log(user4) // {"name": null, "age": 55}
    pprint.log(validateRecord(user4)) // false
    pprint.log(user5) // {"name": null, "age": null}
    pprint.log(validateRecord(user5)) // false
  }

}
