package example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql._
import org.apache.spark.streaming._

object StructuredStreamingConsumer extends App {
  try {
    val spark = SparkSession.builder
      .appName("StructuredKafkaWordCount")
      .getOrCreate()
    import spark.implicits._

    // Create a DataSet representing the stream of input lines from Kafka
    val bootstrapServers = "fake.server.id:9092"
    val subscribeType = "subscribe"
    var topic: String = "/test/stream-volume1-dir/wordcount-stream:wordcount"
    val lines = spark.readStream
      .format("kafka")
    //   .option("kafka.bootstrap.servers", bootstrapServers)
      .option(subscribeType, topic)
      .option("group.id", "testgroup")
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss", false)
      .option("maxOffsetsPerTrigger", 1000)
      .load()
      .selectExpr("CAST(value AS STRING)")
      .as[String]

    // Generate a running word count
    val wordCounts = lines.flatMap(_.split(" ")).groupBy("value").count()

    // Run the query that prints the running counts to the console
    val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .option("checkpointLocation", "/tmp/spark-structured-stream/wordcount")
      .start()

    query.awaitTermination()

  } catch {
    case ex: Exception => {
      ex.printStackTrace() // 標準errorへアウトプット
      System.err.println("exception===>: ...") // 標準errorへアウトプット
    }

  }
}
