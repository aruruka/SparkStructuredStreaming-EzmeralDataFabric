ThisBuild / scalaVersion := "2.12.17"
ThisBuild / organization := "com.hpe"
ThisBuild / version := "0.1.0"

resolvers += "mapr-releases" at "https://repository.mapr.com/maven/"

val sparkVersion = "3.3.0.0-eep-900"
val kafkaVersion = "2.6.1.300-eep-900"

val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided"
val sparkMllib = "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
val sparkSqlKafka = "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion % "provided"
val sparkStreamingKafka = "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion % "provided"
val kafkaEventstreams = "com.mapr.kafka" % "kafka-eventstreams" % "0.2.0.0-eep-900" % "provided"
val rocksdbJni = "org.rocksdb" % "rocksdbjni" % "6.20.3" % "provided"
val maprdbSpark = "com.mapr.db" %% "maprdb-spark" % sparkVersion % "provided"
val kafkaClients = "org.apache.kafka" % "kafka-clients" % kafkaVersion % "provided" exclude("org.apache.zookeeper", "zookeeper")

lazy val sparkStructuredStreamingEzmeralDataFabric = (project in file("."))
.enablePlugins(JavaAppPackaging)
.settings(
    name := "SparkStructuredStreaming-EzmeralDataFabric-Example",
    libraryDependencies ++= Seq(sparkCore, sparkSql, sparkStreaming, sparkMllib, sparkSqlKafka, sparkStreamingKafka, kafkaEventstreams, rocksdbJni, maprdbSpark, kafkaClients)
)