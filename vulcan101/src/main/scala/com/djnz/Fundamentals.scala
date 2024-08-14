package com.djnz

import cats.data.NonEmptyList
import cats.data.NonEmptySet
import cats.implicits._
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.scalatest.Inside
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import vulcan.Avro
import vulcan.Avro.Fixed
import vulcan.AvroError
import vulcan.Codec

/** https://fd4s.github.io/vulcan/
  * https://fd4s.github.io/vulcan/docs/codecs
  */
object Fundamentals {

  object m17 {
    import enumeratum.Enum
    import enumeratum.EnumEntry
    import enumeratum.EnumEntry.Lowercase
    import enumeratum.VulcanEnum

    sealed trait Status extends EnumEntry with Lowercase
    object Status extends Enum[Status] with VulcanEnum[Status] {
      case object Placed extends Status
      case object Paid extends Status
      case object Shipped extends Status
      case object Delivered extends Status

      def values = findValues
    }

    case class Order(number: String, amount: Double, status: Option[Status])
  }

  object m18 {

    sealed trait Status
    object Status {
      case class Placed(date: LocalDateTime) extends Status
      case class Paid(amount: Double) extends Status
      case class Shipped(by: String) extends Status
      case class Delivered(at: Long) extends Status
    }

    case class Order(number: String, amount: Double, status: Status)
    case class Something(whatever: Status)
  }

  object m22 {
    import vulcan.generic._

    @AvroNamespace("com.djnz")
    case class Progress(
      brandId: String,
      playerUUID: String,
      campaignUUID: String,
      version: String,
      update: Progress.Update
    )

    object Progress {
      sealed trait Update
      object Update {
        @AvroNamespace("djnz")
        case class LevelAchieved(levels: NonEmptyList[String]) extends Update

        @AvroNamespace("djnz")
        case class LevelTriggered(levels: NonEmptyList[String]) extends Update

        @AvroNamespace("djnz")
        case class GrantRewards(rewards: NonEmptySet[String]) extends Update

        @AvroNamespace("djnz")
        case class CompleteRewards(rewards: NonEmptySet[String]) extends Update

        @AvroNamespace("djnz")
        case object Expired extends Update

        implicit val c: Codec[Update] = Codec.derive
      }
    }

    implicit val c0 = Codec.derive[Progress.Update]
    val c = Codec.derive[Progress]
  }

}

class Fundamentals extends AnyFunSuite with Matchers with ScalaCheckPropertyChecks with Inside with Tools {

  implicit class EitherValue[E, A](x: Either[E, A]) {
    def value: A = x.getOrElse(sys.error("Supposed to be Right"))
  }

  test("1. non optional/non nullable") {
    Codec[Long]
    Codec[Float]
    Codec[Double]
    Codec[String]
    Codec[Array[Byte]]

    pprint.log(Codec[Int].schema.value) // "int"
    pprint.log(Codec[Instant].schema.value) // {"type":"long","logicalType":"timestamp-millis"}
  }

  test("2. optional/nullable - null or long") {
    val c = Codec[Option[Long]]
    pprint.log(c.schema.value)
  }

  test("3. manual schema building (Java API)") {
    val schema: Schema = SchemaBuilder.builder.booleanType
  }

  test("4. decode value based on schema") {
    val schema: Schema = SchemaBuilder.builder.booleanType
    val x = Codec[Boolean].decode(true, schema)
    pprint.log(x)
  }

  test("5. encode plain value") {
    val x: Either[AvroError, Boolean] = Codec[Boolean].encode(false)
    pprint.log(x)
  }

  test("6. deriving invariant codec") {
    final case class InstallationTime(value: Instant)
    val c: Codec[InstallationTime] = Codec[Instant].imap(InstallationTime(_))(_.value)

    val x: Either[AvroError, c.AvroType] = c.encode(InstallationTime(Instant.now))
    pprint.log(x.value) // long
  }

  test("7. deriving invariant codec with A => Either[E, B], B => A") {
    sealed abstract case class SerialNumber(value: String)

    object SerialNumber {
      def apply(value: String): Either[AvroError, SerialNumber] =
        Either.cond(
          value.length == 12 && value.forall(_.isDigit),
          new SerialNumber(value) {},
          AvroError(s"$value is not a serial number")
        )
    }

    val c: Codec[SerialNumber] = Codec[String].imapError(SerialNumber(_))(_.value)
  }

  test("8. deriving decimal") {
    val c: Codec[BigDecimal] = Codec.decimal(precision = 10, scale = 2)
  }

  test("9. string enum - explicit") {
    sealed trait Fruit
    case object Apple extends Fruit
    case object Banana extends Fruit
    case object Cherry extends Fruit

    val c = Codec.enumeration[Fruit](
      name = "Fruit",
      namespace = "com.example",
      doc = Some("A selection of different fruits"),
      symbols = List("apple", "banana", "cherry"),
      encode = {
        case Apple  => "apple"
        case Banana => "banana"
        case Cherry => "cherry"
      },
      decode = {
        case "apple"  => Right(Apple)
        case "banana" => Right(Banana)
        case "cherry" => Right(Cherry)
        case other    => Left(AvroError(s"$other is not a Fruit"))
      },
      default = Some(Banana)
    )
    pprint.log(c.schema.value)
  }

  test("10. fixed - explicit") {
    sealed abstract case class Pence(value: Byte)

    object Pence {

      def apply(value: Byte): Either[AvroError, Pence] =
        if (0 <= value && value < 100) Right(new Pence(value) {})
        else Left(AvroError(s"Expected pence value, got $value"))

    }

    val c = Codec.fixed[Pence](
      name = "Pence",
      namespace = "com.example",
      size = 1,
      encode = pence => Array[Byte](pence.value),
      decode = bytes => Pence(bytes.head),
      doc = Some("Amount of pence as a single byte")
    )
    pprint.log(c.schema.value)
    val raw: Either[AvroError, Fixed] = c.encode(Pence(13).value)
    pprint.log(raw)

    val fixed = raw.value
    val schema = c.schema.value
    val x: Either[AvroError, Pence] = c.decode(fixed, schema)
    pprint.log(x)
  }

  test("11. generic record (record)") {
    final case class Person(firstName: String, lastName: String, age: Option[Int])

    val c = Codec.record[Person](
      name = "Person",
      namespace = "com.djnz",
      doc = Some("Person with a full name and optional age")
    ) { field =>
      field("fullName", p => s"${p.firstName} ${p.lastName}") *>
        (
          field("firstName", _.firstName),
          field("lastName", _.lastName, doc = Some("the last name")),
          field("age", _.age, default = Some(None))
        ).mapN(Person(_, _, _))
    }
    pprint.log(c.schema.value)

    // generic record
    val x: Either[AvroError, Avro.Record] = c.encode(Person("Jim", "Bim", 33.some))
    val gr: Avro.Record = x.value
    pprint.log(gr)
    val z = c.decode(gr, c.schema.value)
    pprint.log(z)
  }

  test("12. union") {
    sealed trait FirstOrSecond

    final case class First(value: Int) extends FirstOrSecond
    object First {
      implicit val codec: Codec[First] = Codec[Int].imap(apply)(_.value)
    }

    final case class Second(value: String) extends FirstOrSecond
    object Second {
      implicit val codec: Codec[Second] = Codec[String].imap(apply)(_.value)
    }

    val c = Codec.union[FirstOrSecond] { alt =>
      alt[First] |+| alt[Second]
    }

    pprint.log(c.schema.value)
  }

  test("13. enumeratum") {
    import enumeratum.EnumEntry.Lowercase
    import enumeratum.{Enum, EnumEntry, VulcanEnum}
    import vulcan.generic.{AvroDoc, AvroNamespace}

    @AvroNamespace("com.example")
    @AvroDoc("The different card suits")
    sealed trait Suit extends EnumEntry with Lowercase

    object Suit extends Enum[Suit] with VulcanEnum[Suit] {
      case object Clubs extends Suit
      case object Diamonds extends Suit
      case object Hearts extends Suit
      case object Spades extends Suit

      val values = findValues
    }

    val c = Codec[Suit]
    pprint.log(c.schema)
  }

  test("14. enumeratum - string") {
    import enumeratum.values.{StringEnum, StringEnumEntry, StringVulcanEnum}
    import vulcan.generic.{AvroDoc, AvroNamespace}

    @AvroNamespace("com.example")
    @AvroDoc("The available colors")
    sealed abstract class Color(val value: String) extends StringEnumEntry

    object Color extends StringEnum[Color] with StringVulcanEnum[Color] {
      case object Red extends Color("red")
      case object Green extends Color("green")
      case object Blue extends Color("blue")

      val values = findValues
    }

    val c = Codec[Color]
    pprint.log(c.schema.value)
  }

  test("15. enumeratum - int") {
    import enumeratum.values.{IntEnum, IntEnumEntry, IntVulcanEnum}

    sealed abstract class Day(val value: Int) extends IntEnumEntry

    object Day extends IntEnum[Day] with IntVulcanEnum[Day] {
      case object Monday extends Day(1)
      case object Tuesday extends Day(2)
      case object Wednesday extends Day(3)
      case object Thursday extends Day(4)
      case object Friday extends Day(5)
      case object Saturday extends Day(6)
      case object Sunday extends Day(7)

      val values = findValues
    }

    val c = Codec[Day]
    pprint.log(c.schema.value)
  }

  test("16. generic derivation - product (record)") {
    import vulcan.generic._

    @AvroNamespace("com.djnz")
    @AvroDoc("Person with a first name, last name, and optional age")
    final case class Person(firstName: String, lastName: String, age: Option[Int])

    val c = Codec.derive[Person]
    pprint.log(c.schema.value)
  }

  test("17. product with coproduct inside") {
    import Fundamentals.m17._
    import vulcan.generic._

    val c = Codec.derive[Order]
    pprint.log(c.schema.value)
  }

  test("18. product with coproduct inside") {
    import Fundamentals.m18._
    import vulcan.generic._

    implicit val cdt: Codec[LocalDateTime] = Codec[Instant]
      .imap { instant =>
        LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
      } { ldt =>
        ldt.toInstant(ZoneOffset.UTC)
      }
    implicit val sc: Codec[Status] = Codec.derive[Status]
    val c = Codec.derive[Order]
    pprint.log(c.schema.value)
  }

  test("19. product with coproduct inside 2") {
    import Fundamentals.m18._
    import vulcan.generic._

    implicit val cdt: Codec[LocalDateTime] = Codec[Instant]
      .imap { instant =>
        LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
      } { ldt =>
        ldt.toInstant(ZoneOffset.UTC)
      }
    implicit val sc = Codec.derive[Status]
    val c = Codec.derive[Something]
    pprint.log(c.schema.value)
  }

  test("20. generic derivation - coproduct") {
    import vulcan.generic._
    sealed trait FirstOrSecondOrThird

    @AvroNamespace("com.djnz")
    final case class First(value: Int) extends FirstOrSecondOrThird

    @AvroNamespace("com.djnz")
    final case class Second(value: String) extends FirstOrSecondOrThird

    @AvroNamespace("com.djnz")
    final case class Third(value: Double) extends FirstOrSecondOrThird

    val c = Codec.derive[FirstOrSecondOrThird]
    pprint.log(c.schema.value)
  }

  test("21. shapeless coproduct") {
    import shapeless.{:+:, CNil}
    import vulcan.generic._

    @AvroNamespace("com.djnz")
    case class Person(name: String, age: Int)
    implicit val cp = Codec.derive[Person]

    val c = Codec[Person :+: Int :+: String :+: CNil]
    pprint.log(c.schema.value.toString(true))
    pprint.log(represent(c.schema.value.toString))
  }

  test("22. shapeless coproduct") {
    import Fundamentals.m22._
    pprint.log(c.schema.value.toString(true))
    pprint.log(represent(c.schema.value.toString))
  }
}
