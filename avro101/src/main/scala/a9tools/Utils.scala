package a9tools

import java.nio.file.{Files, Paths}

object Utils {

  def absolutePathFromResources(name: String): String =
    getClass.getClassLoader.getResource(name).getFile

  // JDK 11
  def contentsFromResources(name: String): String =
    Files.readString(Paths.get(absolutePathFromResources(name)))

}
