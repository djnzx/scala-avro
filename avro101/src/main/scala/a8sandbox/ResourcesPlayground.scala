package a8sandbox

import java.io.File
import java.net.URL

object ResourcesPlayground extends App {

  val c: Class[_] = getClass
  val cl: ClassLoader = c.getClassLoader
  val r: URL = cl.getResource("1/schema3.json")
  val path: String = r.getFile
  pprint.log(path)
  val f = new File(path)
  pprint.log(f)
}
