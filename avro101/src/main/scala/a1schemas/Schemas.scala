package a1schemas

import org.apache.avro.SchemaBuilder

object Schemas {

  /** create schema programatically */
  // format: off
  val sPerson1 = SchemaBuilder.record("person").fields()
    .name("name").`type`.stringType.noDefault
    .name("age").`type`.nullable.intType.noDefault
    .endRecord

  val sPerson2 = SchemaBuilder.record("person").fields()
    .name("name").`type`.stringType.noDefault
    .name("age").`type`.nullable.intType.noDefault
    .name("skill").`type`.stringType.noDefault
    .endRecord

  val addressSchema = SchemaBuilder.record("address").fields()
    .name("street").`type`.stringType.noDefault
    .name("house").`type`.longType().noDefault
    .endRecord()

  val sPerson3 = SchemaBuilder.record("person2").fields()
    .name("name").`type`.stringType.noDefault
    .name("age").`type`.nullable.intType.noDefault
    .name("address").`type`(addressSchema).noDefault
    .endRecord
  // format: on

}
