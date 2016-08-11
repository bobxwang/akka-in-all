name := "akka-in-all"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Akka Repository" at "http://repo.akka.io/releases/"

val akkaverstion = "2.4.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % akkaverstion,
  "com.typesafe.akka" % "akka-slf4j_2.11" % akkaverstion,
  "com.typesafe.akka" % "akka-remote_2.11" % akkaverstion,
  "com.typesafe.akka" % "akka-agent_2.11" % akkaverstion,
  "com.typesafe.akka" % "akka-cluster_2.11" % akkaverstion,
  "com.typesafe.akka" % "akka-cluster-metrics_2.11" % akkaverstion
)