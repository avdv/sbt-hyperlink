package sbthyperlink

import scala.util.matching.Regex
import java.io.File
import utest._

object HyperlinkPluginTest extends TestSuite {

  val tests = Tests {
    'testFileAction - {
      val r = Default.regex(new File("/abc"))
      val m = r.findFirstMatchIn("/abc/test/hello.scala:55:3: deprecated")

      assert(m.isDefined)

      val (link, text) = FileAction(m.get).get

      assert(link == "file:///abc/test/hello.scala", text == "test/hello.scala:55:3")
    }
    'testTermlinkAction - {
      val r = Default.regex(new File("/abc"))
      val m = r.findFirstMatchIn("/abc/test/hello.scala:55:3: deprecated")

      assert(m.isDefined)

      val (link, text) = TermlinkAction(m.get).get

      assert(link == "termlink:///abc/test/hello.scala:55:3", text == "test/hello.scala:55:3")
    }
    'testRelativeLink - {
      val m = Default.regex(new File("/foo/bar")).findFirstMatchIn("/foo/bar/test/hello.scala:55:3:")

      assert(m.isDefined)

      val matched = m.get
      val basedir = matched.group("basedir")
      val subpath = matched.group("subpath")
      val pos = matched.group("pos")

      assert(subpath == "test/hello.scala", pos == "55:3")
    }
    'testDefaultRegex - {
      val m = Default.regex(new File("/abc")).findFirstMatchIn(
        """[info] /abc/src/main/scala/Main.scala:48: Missing final modifier on case class"""
      )

      assert(m.isDefined)

      val matched = m.get
      val basedir = matched.group("basedir")
      val subpath = matched.group("subpath")
      val pos = matched.group("pos")

      assert(basedir == "/abc", subpath == "src/main/scala/Main.scala", pos == "48")
    }
  }
}
