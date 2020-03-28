package sbthyperlink

import java.io.PrintWriter

import sbt.{ Def, _ }
import sbt.Keys._
import sbt.plugins.CorePlugin
import sbt.internal._
import sbt.internal.util.MainAppender._
import sbt.internal.util.ConsoleAppender

import scala.util.matching.Regex

trait HyperlinkAction {
  def apply(m: Regex.Match): Option[(String, String)]
}

/** Create a `termlink://` link to the matched file
  *
  * Use the relative path based on the `baseDirectory` as the link text.
  */
object TermlinkAction extends HyperlinkAction {
  override def apply(m: Regex.Match) = m match {
    case Regex.Groups(basedir, subpath, pos) =>
      Some(s"termlink://$basedir/$subpath:$pos" -> s"$subpath:$pos")
    case _ => None
  }
}

/** Create a `file://` link the given absolute path, shortening the path in the link text.
  *
  * @param m a Regex.Match with groups: basedir, path, file, pos
  * @return a tuple of (fileUri, linkText)
  */
object FileAction extends HyperlinkAction {
  override def apply(m: Regex.Match) = m match {
    case Regex.Groups(basedir, subpath, pos) =>
      Some(s"file://$basedir/$subpath" -> s"$subpath:$pos")
    case Regex.Groups(basedir, path, file, pos) =>
      Some(s"file://$basedir/$path/$file" → s"$path/$file:$pos")
    case _ => None
  }
}

object Default {
  def regex(basedir: File): Regex = {
  s"""(?x)
         # basedir
         (${Regex.quote(basedir.getAbsolutePath)})
         /+
         # subpath + file - relative to the basedir
         ((?:[^/:]+/+)*  [^/:]+)
         :
         # pos - LINE[:COLUMN]
         (\\d+(?::\\d+)?)
         # a colon next
         (?=:)""".r("basedir", "subpath", "pos")
  }
}

object HyperlinkPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = CorePlugin

  object autoImport {
    val hyperlinkRegex = settingKey[Regex]("A regex which matches specific parts to be hyperlinked")
    val hyperlinkAction = settingKey[HyperlinkAction](
      """This function is given each match of the regular expression.
        |
        |It should return a tuple of Strings, the first is the target URI and the
        |second is the link text, or None if the match should not be hyperlinked.
      """.stripMargin)
  }

  import autoImport._

  private def hyperlink(linkText: (String, String)): String = {
    val (href, text) = linkText
    s"\033]8;;${href}\007${text}\033]8;;\007"
  }

  override lazy val projectSettings = Seq(
    hyperlinkRegex := Default.regex(baseDirectory.value),
    hyperlinkAction := FileAction,
    logManager := {
      LogManager.withScreenLogger {
        (_: ScopedKey[_], state: State) ⇒
          val extracted = Project.extract(state)
          val basedir = extracted.get(baseDirectory)
          val action: HyperlinkAction = extracted.get(hyperlinkAction)
          val regex: Regex = extracted.get(hyperlinkRegex)

          defaultScreen(ConsoleOut.printWriterOut(new PrintWriter(System.out) {
            private def filter(s: String) =
              if (ConsoleAppender.formatEnabledInEnv)
                regex.replaceSomeIn(s, action(_).map(hyperlink))
              else
                s

            override def print(s: String): Unit = super.print(filter(s))
          }))
      }
    }
  )
}
