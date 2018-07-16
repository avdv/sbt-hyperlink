package sbthyperlink

import scala.util.matching.Regex

import utest._

object HyperlinkPluginTest extends TestSuite {
  val tests = Tests {
    'testFileAction - {
      val r = "(/abc)/(test)/(hello.scala):(\\d+:\\d+)".r("basedir", "path", "file", "pos")
      val m = r.findFirstMatchIn("/abc/test/hello.scala:55:3")

      assert(m.isDefined)

      val (link, text) = FileAction(m.get).get

      assert(link == "file:///abc/test/hello.scala", text == "test/hello.scala:55:3")
    }
    'testTermlinkAction - {
      val r = "(/abc)/(test)/(hello.scala):(\\d+:\\d+)".r("basedir", "path", "file", "pos")
      val m = r.findFirstMatchIn("/abc/test/hello.scala:55:3")

      assert(m.isDefined)

      val (link, text) = TermlinkAction(m.get).get

      assert(link == "termlink:///abc/test/hello.scala:55:3", text == "test/hello.scala:55:3")
    }
  }
}
