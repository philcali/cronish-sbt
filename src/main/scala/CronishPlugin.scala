import sbt._

import Keys._
import complete.DefaultParsers._

import com.github.philcali.cronish.dsl._

object CronishPlugin extends Plugin {
  val CronishConf = config("cronish") 

  val tasks = SettingKey[Seq[Scheduled]]("tasks", "Actively defined crons.")

  val list = TaskKey[Unit]("list", "Lists all the active tasks")

  val addSh = InputKey[Unit]("add-sh", 
              "Adds a cronish task that executes a system command.")

  val addSbt = InputKey[Unit]("add-sbt",
              "Adds a sbt task to be executed at a defined interval.")

  val findNext = TaskKey[Unit]("next",
              "Iterates through active jobs and prints out next job.")

  object add {
    def > (work: ProcessBuilder) = 
      job (work !) describedAs "process %s".format(work)
    def sh (cmd: String) = 
      job(cmd !) describedAs "sh action %s".format(cmd)
    def sbt (cmd: String, st: State) = job {
      Command.process(cmd, st) 
    } describedAs "sbt action %s".format(cmd)
  }

  private def cronishListTask = (streams) map { s =>
    Scheduled.active.map(fullReport).foreach(s.log.info(_))
  }

  private def fullReport(sched: Scheduled) = {
    val desc = sched.task.description match {
      case Some(str) => str
      case _ => "A job that"
    }

    val crondef = sched.definition.full      

    val at = sched.definition.nextTime

    "%s runs %s next run: %s" format(desc, crondef, at)
  }

  private val cronishAddDef = (parsedTask: TaskKey[(String, Seq[Char])]) => {
    (parsedTask, state, streams) map { case ( (es, crons), st, s ) =>
      val cronD = "every%s" format (crons.mkString)

      add sbt (es, st) runs cronD    

      s.log.info("Adding %s to be run %s".format(es, cronD))
    }
  }

  private val generalParser = token(Space ~ "runs" ~ Space) ~> "every" ~> (any +)

  private val cronishParser = (s: State) => {
    Space flatMap { _ =>
      matched(s.combinedParser) ~ generalParser
    }
  }

  val cronishSettings: Seq[Setting[_]] = inConfig(CronishConf) (Seq (
    tasks := List[Scheduled](),
 
    addSh <<= inputTask { argTask =>
      (argTask, streams) map { (args, s) =>
        val Array(cmd, crons) = args.mkString(" ").split(" runs ")

        add sh cmd runs crons

        s.log.info("Successfully added %s to run %s".format(cmd, crons))
      }
    },

    addSbt <<= InputTask(cronishParser)(cronishAddDef),

    list <<= cronishListTask,

    findNext <<= (streams) map { s => 
      val jobs = Scheduled.active

      val trans = (d: Scheduled) => d.definition.nextTime

      val attempt = jobs.sortWith(trans(_) < trans(_)).headOption

      val report = attempt.map(fullReport).getOrElse("No jobs")

      s.log.info(report)
    }
  )) ++ Seq (
    list in CronishConf,
    addSh in CronishConf,
    addSbt in CronishConf,
    findNext in CronishConf
  ).map (aggregate in _ := false)
}
