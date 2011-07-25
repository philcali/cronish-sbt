import sbt._

import Keys._

import com.github.philcali.cronish.dsl.Scheduled

object CronishKeys {
  val Cronish = config("cronish") 

  val tasks = SettingKey[Seq[Scheduled]]("tasks", "Actively defined crons.")
  val list = TaskKey[Unit]("list", "Lists all the active tasks")

  val addSh = InputKey[Unit]("add-sh", 
              "Adds a cronish task that executes a system command.")
  val addSbt = InputKey[Unit]("add-sbt",
              "Adds a sbt task to be executed at a defined interval.")
}
