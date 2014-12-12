name := "spark_cluster_sample"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3",
  "org.apache.spark" %% "spark-core" % "1.1.1",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-remote" % "2.2.3"
)     

play.Project.playScalaSettings
