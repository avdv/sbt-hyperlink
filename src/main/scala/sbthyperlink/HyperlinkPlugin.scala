package sbthyperlink

import java.io.PrintWriter

import sbt.{ Def, _ }
import sbt.Keys._
import sbt.plugins.CorePlugin
import sbt.internal._
import sbt.internal.util.MainAppender._
import sbt.internal.util.ConsoleAppender

import scala.util.matching.Regex

object HyperlinkPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = CorePlugin

  type HyperlinkFunction = Regex.Match => Option[(String, String)]

  object autoImport {
    val hyperlinkRegex = settingKey[Regex]("A regex which matches specific parts to be hyperlinked")
    val hyperlinkAction = settingKey[HyperlinkFunction](
      """This function is given each match of the regular expression.
        |
        |It should return a tuple of Strings, the first is the target URI and the
        |second is the link text, or None if the match should not be hyperlinked.
      """.stripMargin)
  }

  /** Create a `file://` link the given absolute path, shortening the path in the link text.
    *
    * @param m a Regex.Match with groups: basedir, path, file, pos
    * @return a tuple of (fileUri, linkText)
    */
  def fileLink(m: Regex.Match) = m match {
    case Regex.Groups(basedir, path, file, pos) =>
      Some(s"file://$basedir/$path/$file" → s"$path/$file:$pos")
  }

  import autoImport._

  private val hyperlink: ((String, String)) => String = {
    case (href: String, text: String) => s"\033]8;;${href}\007${text}\033]8;;\007"
  }

  override lazy val projectSettings = Seq(
    hyperlinkRegex := {
      val basedir = baseDirectory.value
      s"""(?x)
         # basedir
         (${Regex.quote(basedir.getAbsolutePath)})
         # path - relative to the basedir
         ((?:/+[^/:]+)*)
         /
         # file - the name of the file
         ([^/:]+)
         :
         # pos - LINE[:COLUMN]
         (\\d+(?::\\d+)?)
         # a colon next
         (?=:)""".stripMargin.r("basedir", "path", "file", "pos")
    },
    hyperlinkAction := fileLink,
    logManager := {
      LogManager.withScreenLogger {
        (_: ScopedKey[_], state: State) ⇒
          val extracted = Project.extract(state)
          val basedir = extracted.get(baseDirectory)
          val action: HyperlinkFunction = extracted.get(hyperlinkAction)
          val regex: Regex = extracted.get(hyperlinkRegex)

          defaultScreen(ConsoleOut.printWriterOut(new PrintWriter(System.out) {
            private def filter(s: String) =
              if (ConsoleAppender.formatEnabledInEnv)
                regex.replaceSomeIn(s, action.andThen(o => o.map(hyperlink)))
              else
                s

            override def print(s: String): Unit = super.print(filter(s))
          }))
      }
    }
  )
}
