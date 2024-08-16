package a9tools

import cats.implicits.catsSyntaxOptionId
import cats.implicits.toBifunctorOps
import cats.implicits.toFunctorOps
import java.io.ByteArrayOutputStream
import java.io.File
import org.apache.avro.Schema
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import scala.collection.JavaConverters._
import scala.util.Try

object Tools {


  /** write to file with schema */

  def readFrom(file: File, maybeSchema: Option[Schema] = None): List[GenericRecord] = {
    val gr = maybeSchema.fold(
      new GenericDatumReader[GenericRecord]
    ) { schema =>
      new GenericDatumReader[GenericRecord](schema)
    }
    val r = new DataFileReader[GenericRecord](file, gr)
    val x = r.iterator().asScala.toList
    r.close()
    x
  }

  /** read WITH schema check enforced */
  def readFrom(file: File, schema: Schema): List[GenericRecord] =
    readFrom(file, schema.some)

}
