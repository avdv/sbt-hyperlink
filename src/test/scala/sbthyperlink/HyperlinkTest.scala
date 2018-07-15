package sbthyperlink

import scala.util.matching.Regex

import utest._

object HyperlinkTest extends TestSuite {
  val tests = Tests {
    'testRegexMatch - {
      val r = "(/abc)/(test)/(hello.scala):(\\d+:\\d+)".r("basedir", "path", "file", "pos")
      val m = r.findFirstMatchIn("/abc/test/hello.scala:55:3")

      assert(m.isDefined)

      val (link, text) = HyperlinkPlugin.fileLink(m.get).get

      assert(link == "file:///abc/test/hello.scala", text == "test/hello.scala:55:3")
    }
  }
}
