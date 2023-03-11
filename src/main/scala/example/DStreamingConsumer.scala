package example

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._
import org.apache.spark.sql._
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, LocationStrategies, KafkaUtils}
import org.apache.kafka.clients.consumer.ConsumerRecord


object DStreamingConsumer extends App {
  try {
    val spark = SparkSession.builder
      .appName("DStreamingKafkaWordCount")
      .getOrCreate()
    import spark.implicits._

    val sparkConf = spark.sparkContext
    val sparkStreamingContext = new StreamingContext(sparkConf, Seconds(2))
    sparkStreamingContext.checkpoint("/tmp/spark-structured-stream/wordcount")
    // Create direct kafka stream with brokers and topics
    var topicSet = "/test/stream-volume1-dir/wordcount-stream:wordcount".split(",").toSet
    val bootstrapServers = "fake.server.id:9092"
    val commonParams = Map[String, Object](
      "bootstrap.servers" -> bootstrapServers,
      "auto.offset.reset" -> "earliest",
      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "group.id" -> "testgroup",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val protocol = "MAPRSASL"
    val isUsingSsl = protocol.endsWith("SSL")
    val additionalSslParams = if (isUsingSsl) {
      Map(
        "ssl.truststore.location" -> "/etc/cdep-ssl-conf/CA_STANDARD/truststore.jks",
        "ssl.truststore.password" -> "cloudera"
      )
    } else {
      Map.empty
    }

    val kafkaParams = commonParams ++ additionalSslParams
    val messages: InputDStream[ConsumerRecord[String, String]] =
      KafkaUtils.createDirectStream[String, String](
        sparkStreamingContext,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams)
      )

    // Get the lines, split them into words, count the words and print
    val lines = messages.map(_.value())
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1L)).reduceByKey(_ + _)
    wordCounts.print()

    // Start the computation
    sparkStreamingContext.start()
    sparkStreamingContext.awaitTermination()

  } catch {
    case ex: Exception => {
      ex.printStackTrace() // 標準errorへアウトプット
      System.err.println("exception===>: ...") // 標準errorへアウトプット
    }
  }
}
