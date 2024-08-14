package com.djnz

trait Tools {

  def represent(rawJson: String): String =
    io.circe.parser.parse(rawJson)
      .map(_.spaces2)
      .getOrElse(throw new RuntimeException("Expected to be a Valid JSON"))

}
