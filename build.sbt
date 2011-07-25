sbtPlugin := true

name := "cronish-sbt"

version := "0.0.2"

organization := "com.github.philcali"

libraryDependencies <+= (organization) (_ %% "cronish" % "0.0.1")

publishTo := Some("Scala Tools Nexus" at 
                  "http://nexus.scala-tools.org/content/repositories/releases/")

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
