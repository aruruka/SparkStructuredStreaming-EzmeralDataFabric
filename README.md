# SparkStructuredStreaming-EzmeralDataFabric

An example using Spark Structured Stream on Ezmeral Data Fabric

## [](#introduction)序章

この文章は以下のことをデモンストレーションする。

1. Scala で Spark Structured Streaming の example アプリケーションを作る。

    ビルドツールは sbt を使用する。Java と Maven より Scala と sbt に詳しいので…
    ビルドのコマンドライン操作も記録する。

2. 作った Spark Structured Streaming アプリケーションを HPE Ezmeral Data Fabric 上で実行する。

    HPE Ezmeral Data Fabric Ecosystem Pack 8.0.0 より [Kafka Stream ライブラリ](https://docs.datafabric.hpe.com/72/Kafka/KStreams/KStreams.html)が加わった。
    Kafka Stream は Apache Kafka プロジェクトから派生したプロジェクトで、独自の Stream 実装が備わっている。
    やや理解しにくいかも知れないが、以下のような考え方ができる:
    以前、スケールアップ できる Stream 系アプリケーションを作るために、Spark や Flink を使って Kafka とコミュニケーションするというやり方しかなかった。
    今は、Kafka Stream という新しい Stream フレームワークができた。
    Kafka Stream で作った Stream アプリ でも スケールアップ できるし、恐らく Spark の RDD に基づく プログラミング モデル とも違う。とにかく新しい Stream フレームワークだ。選択肢が増えた。

    そこで、HPE Ezmeral Data Fabric Ecosystem Pack もその Kafka Stream を取り入れた。
    もちろん昔 Spark-Kafka で作ったアプリは新しい HPE Ezmeral Data Fabric でも動けるが、アプリ の dependency ライブラリ を少し変えなければならない。ソースコード も変更する必要があるかも知れない。

    ソースコードでは Stream のオブジェクトを作る時に、「kafka.bootstrap.servers」という プロパティ が必ず必要となった。
    今回は「kafka.bootstrap.servers」なしで Stream を作るとエラーが出るところもデモンストレーションする。

3. HPE Ezmeral Data Fabric で Stream の トピック を作り、メッセージを書き込む。

    `maprcli` コマンドラインで Stream と Topic を作る。
    Kafka のコマンドライン ツールの kafka-console-producer.sh を使って Topic へメッセージを書き込む。


## [](#download-sorce-code)ソースコードをダウンロードし、ビルドする

### SBT のインストール

SBT をインストール ➡ [Installing sbt on Linux](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html)
💡Note: **Installing from SDKMAN** の方法でインストールすることをおすすめする。
sdkman で JDK をインストールするなら[Temurin (Eclipse)](https://projects.eclipse.org/projects/adoptium.temurin)の JDK 11 もしくは JDK 17 をインストールしましょう。

sbt に proxy を通させたい場合は以下👇のようにする:

```bash
cd $(dirname $(which sbt))/../conf
```

すると、<ins>sbtopts</ins> という ファイル が sbt の ホーム ディレクトリの <ins>conf</ins> ディレクトリー にあると分かる。
その <ins>sbtopts</ins> 以下の内容を追加する:

```
-Dhttp.proxyHost={プロキシサーバーホスト}
-Dhttp.proxyPort={プロキシポート}
-Dhttps.proxyHost={プロキシサーバーホスト}
-Dhttps.proxyPort={プロキシポート}
```

### Spark Structured Streaming example アプリのソースコードをダウンロード

```shell
curl -L 'https://github.com/aruruka/SparkStructuredStreaming-EzmeralDataFabric/archive/refs/heads/main.zip' -O ./
```

### アプリ の ソースコード を コンパイル し、パッケージ する

👇 ダウンロードしたら、「main.zip」という ファイル になる。

```shell
unzip main.zip
cd SparkStructuredStreaming-EzmeralDataFabric-main/
ls -1
```

Output: 

```
README.md
build.sbt     # 👈 この プロジェクト の SBT コンフィグ ファイル
built_package
project
src
target
```

<ins>SparkStructuredStreaming-EzmeralDataFabric-main/</ins>に`cd`してから、`sbt`と入力して、sbt の interactive console に入る。

sbt の interactive console は以下👇のような感じ:

```
$ sbt
copying runtime jar...
[info] Updated file /tmp/downloads/SparkStructuredStreaming-EzmeralDataFabric-main/project/build.properties: set sbt.version to 1.6.1
[info] welcome to sbt 1.6.1 (Ubuntu Java 11.0.17)
[info] loading settings for project sparkstructuredstreaming-ezmeraldatafabric-main-build from plugins.sbt ...
[info] loading project definition from /tmp/downloads/SparkStructuredStreaming-EzmeralDataFabric-main/project
[info] loading settings for project sparkStructuredStreamingEzmeralDataFabric from build.sbt ...
[info] set current project to SparkStructuredStreaming-EzmeralDataFabric-Example (in build file:/tmp/downloads/SparkStructuredStreaming-EzmeralDataFabric-main/)
[info] sbt server started at local:///home/raymondyan/.sbt/1.0/server/fa291765ca0b43adee2d/sock
[info] started sbt server
sbt:SparkStructuredStreaming-EzmeralDataFabric-Example>
```

ここで、interactive console の中で `compile` と入力して、コンパイルする。
以下のような出力となる:

```
https://repo1.maven.org/maven2/org/apache/arrow/arrow-format/7.0.0/arrow-format-7.0.0.jar
  100.0% [##########] 107.4 KiB (122.7 KiB / s)
https://repo1.maven.org/maven2/org/typelevel/spire-util_2.12/0.17.0/spire-util_2.12-0.17.0.jar
  100.0% [##########] 34.3 KiB (56.4 KiB / s)
https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-slf4j-impl/2.17.2/log4j-slf4j-impl-2.17.2.jar
  100.0% [##########] 23.7 KiB (39.4 KiB / s)
https://repo1.maven.org/maven2/org/codehaus/jettison/jettison/1.1/jettison-1.1.jar
  100.0% [##########] 66.2 KiB (77.8 KiB / s)
https://repository.mapr.com/maven/org/apache/kafka/connect-api/2.6.1.300-eep-900/connect-api-2.6.1.300-eep-900.jar
  100.0% [##########] 101.0 KiB (74.1 KiB / s)
https://repo1.maven.org/maven2/org/glassfish/jersey/inject/jersey-hk2/2.36/jersey-hk2-2.36.jar
  100.0% [##########] 76.5 KiB (90.6 KiB / s)
https://repository.mapr.com/maven/org/apache/hadoop/thirdparty/hadoop-shaded-protobuf_3_11/1.1.1.0-eep-900/hadoop-shad…
  100.0% [##########] 1.6 MiB (397.1 KiB / s)
https://repository.mapr.com/maven/org/apache/zookeeper/zookeeper-jute/3.5.6.0-mapr-2210/zookeeper-jute-3.5.6.0-mapr-22…
  100.0% [##########] 243.4 KiB (160.9 KiB / s)
https://repo1.maven.org/maven2/org/specs2/specs2-common_2.12/2.4.17/specs2-common_2.12-2.4.17.jar
  100.0% [##########] 1021.4 KiB (388.4 KiB / s)
https://repository.mapr.com/maven/org/apache/hadoop/hadoop-hdfs/2.7.6.200-eep-810/hadoop-hdfs-2.7.6.200-eep-810.jar
  100.0% [##########] 8.0 MiB (454.4 KiB / s)
https://repo1.maven.org/maven2/org/rocksdb/rocksdbjni/6.20.3/rocksdbjni-6.20.3.jar
  100.0% [##########] 34.4 MiB (842.8 KiB / s)
[info] Fetched artifacts of
[info] compiling 1 Scala source to /tmp/downloads/SparkStructuredStreaming-EzmeralDataFabric-main/target/scala-2.12/classes ...
[info] Non-compiled module 'compiler-bridge_2.12' for Scala 2.12.17. Compiling...
[info]   Compilation completed in 12.374s.
[success] Total time: 230 s (03:50), completed Feb 25, 2023, 9:36:58 PM
sbt:SparkStructuredStreaming-EzmeralDataFabric-Example>
```

そして、`package` と入力して、プログラムを jar ファイル に パッケージ する。
パッケージ したら、成果物の jar ファイルは <ins>target/scala-2.12/</ins> にある。

## [](#run-spark-structured-stream-example-app)Spark Structured Streaming example アプリを実行する

この Spark Structured Streaming example アプリを実行する前に、予め後で記載されている 「Stream の トピック作成」 のステップを完了させましょう。
Stream が存在しないと、アプリは失敗する。
あと、ことアプリは <ins>/tmp/spark-structured-stream/wordcount</ins> を チェック ポイント として使うので、予めディレクトリーを作っておく👇。

```shell
sudo -E -u mapr \
  hadoop fs -mkdir -p /tmp/spark-structured-stream/wordcount
```

### kafka.bootstrap.servers プロパティありで、成功する例

予め、[HPE Ezmeral Data Fabric Ecosystem Pack 9.0.0](https://docs.datafabric.hpe.com/72/DevelopmentGuide/MavenArtifactsEEP900.html) の ヴァージョン に合わせた dependency ライブラリ で ビルド した jar ファイルが、さっき ダウンロード した zip ファイル にある。
<ins>built_package</ins> にある。

- sparkstructuredstreaming-ezmeraldatafabric-example_2.12-0.1.0.jar

    ☝こっちが「kafka.bootstrap.servers」のプロパティを渡してあり、正常に動けるほう。

- sparkstructuredstreaming-ezmeraldatafabric-example_2.12-0.1.0.jar

    ☝こっちが「kafka.bootstrap.servers」のプロパティを渡してなく、エラーとなるほう。

以下のような`spark-submit`コマンドラインで実行できる:

```shell
export SPARK_HOME=/opt/mapr/spark/spark-3.3.1

sudo -E -u mapr \
SPARK_KAFKA_VERSION=0.10 \
$SPARK_HOME/bin/spark-submit \
--class example.StructuredStreamingConsumer \
--master yarn \
--deploy-mode client \
./sparkstructuredstreaming-ezmeraldatafabric-example_2.12-0.1.0.jar
```

実行したら、以下のような出力となる:

```
# sudo -E -u mapr \
> SPARK_KAFKA_VERSION=0.10 \
> $SPARK_HOME/bin/spark-submit \
> --class example.StructuredStreamingConsumer \
> --master yarn \
> --deploy-mode client \
> ./target/scala-2.12/sparkstructuredstreaming-ezmeraldatafabric-example_2.12-0.1.0.jar
-------------------------------------------
Batch: 0
-------------------------------------------
+-----+-----+
|value|count|
+-----+-----+
+-----+-----+

-------------------------------------------
Batch: 1
-------------------------------------------
+------+-----+
| value|count|
+------+-----+
|word27|    2|
|word28|    1|
|word29|    1|
+------+-----+

^C
```

### kafka.bootstrap.servers プロパティなしで、エラーとなる例

以下のスクリーンショットのように Kafka の Stream を初期化させる時に「kafka.bootstrap.servers」というプロパティを外す。

<a href="https://ibb.co/8gGQXt2"><img src="https://i.ibb.co/Jj8SCg2/HPE-Spark-Structured-Streaming-Bootstrap-Server-Error.png" alt="HPE-Spark-Structured-Streaming-Bootstrap-Server-Error" border="0"></a>

```
# sudo -E -u mapr \
> SPARK_KAFKA_VERSION=0.10 \
> $SPARK_HOME/bin/spark-submit \
> --class example.StructuredStreamingConsumer \
> --master yarn \
> --deploy-mode client \
> ./target/scala-2.12/sparkstructuredstreaming-ezmeraldatafabric-example-without_bootstrapserver_2.12-0.1.0.jar
java.lang.IllegalArgumentException: Option 'kafka.bootstrap.servers' must be specified for configuring Kafka consumer
        at org.apache.spark.sql.kafka010.KafkaSourceProvider.validateGeneralOptions(KafkaSourceProvider.scala:323)
        at org.apache.spark.sql.kafka010.KafkaSourceProvider.org$apache$spark$sql$kafka010$KafkaSourceProvider$$validateStreamOptions(KafkaSourceProvider.scala:343)
        at org.apache.spark.sql.kafka010.KafkaSourceProvider.sourceSchema(KafkaSourceProvider.scala:71)
        at org.apache.spark.sql.execution.datasources.DataSource.sourceSchema(DataSource.scala:236)
        at org.apache.spark.sql.execution.datasources.DataSource.sourceInfo$lzycompute(DataSource.scala:118)
        at org.apache.spark.sql.execution.datasources.DataSource.sourceInfo(DataSource.scala:118)
        at org.apache.spark.sql.execution.streaming.StreamingRelation$.apply(StreamingRelation.scala:34)
        at org.apache.spark.sql.streaming.DataStreamReader.loadInternal(DataStreamReader.scala:168)
        at org.apache.spark.sql.streaming.DataStreamReader.load(DataStreamReader.scala:144)
        at example.StructuredStreamingConsumer$.delayedEndpoint$example$StructuredStreamingConsumer$1(StructuredStreamingConsumer.scala:27)
        at example.StructuredStreamingConsumer$delayedInit$body.apply(StructuredStreamingConsumer.scala:9)
        at scala.Function0.apply$mcV$sp(Function0.scala:39)
        at scala.Function0.apply$mcV$sp$(Function0.scala:39)
        at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:17)
        at scala.App.$anonfun$main$1$adapted(App.scala:80)
        at scala.collection.immutable.List.foreach(List.scala:431)
        at scala.App.main(App.scala:80)
        at scala.App.main$(App.scala:78)
        at example.StructuredStreamingConsumer$.main(StructuredStreamingConsumer.scala:9)
        at example.StructuredStreamingConsumer.main(StructuredStreamingConsumer.scala)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:566)
        at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
        at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:961)
        at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
        at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
        at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
        at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1063)
        at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1072)
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
exception===>: ...
```
## [](#create-topic-send-message)HPE Ezmeral Data Fabric で Stream の トピック を作り、メッセージを書き込む

### Stream 用の Volume を作る

```bash
sudo -E -u mapr \
  maprcli volume create -name test.teststreamvolume1 -path /test/stream-volume1-dir
# ☝ test.teststreamvolume1 という名の Volume を作り、 MapR FS 中の /test/stream-volume1-dir にマウント
sudo -E -u mapr \
  hadoop fs -ls -d -h /test/stream-volume1-dir

sudo -E -u mapr \
  hadoop mfs -ls /test/stream-volume1-dir
```

### Stream と Topic を作る

```bash
sudo -E -u mapr \
  maprcli stream create -path /test/stream-volume1-dir/wordcount-stream
# Stream の path が /test/stream-volume1-dir/wordcount-stream となる
sudo -E -u mapr \
  maprcli stream edit -path /test/stream-volume1-dir/wordcount-stream \
  -produceperm p -consumeperm p -topicperm p
# 全てのユーザーに読み書きの権限を渡す
```

```bash
sudo -E -u mapr \
  maprcli stream topic create -path /test/stream-volume1-dir/wordcount-stream \
  -topic wordcount
# Topic の名前が wordcount となる
sudo -E -u mapr \
  maprcli stream topic list -path /test/stream-volume1-dir/wordcount-stream
```

### メッセージを書き込む

kafka-console-producer.sh という コマンドライン ツール を使う。

```bash
export KAFKA_HOME=/opt/mapr/kafka/kafka-2.6.1

sudo -E -u mapr \
$KAFKA_HOME/bin/kafka-console-producer.sh \
--broker-list fake.server.id:9092 \
--topic /test/stream-volume1-dir/wordcount-stream:wordcount
```

実行したら interactive console が見え、下記のように適当に言葉を入力する:

```
# sudo -E -u mapr \
> $KAFKA_HOME/bin/kafka-console-producer.sh \
> --broker-list fake.server.id:9092 \
> --topic /test/stream-volume1-dir/wordcount-stream:wordcount
>word27 word28 word27 word29
```

💡Note: <ins>word27 word28 word27 word29</ins> が入力された言葉。

すると、Spark アプリのほうで以下の出力が見える:

```
-------------------------------------------
Batch: 0
-------------------------------------------
+-----+-----+
|value|count|
+-----+-----+
+-----+-----+

-------------------------------------------
Batch: 1
-------------------------------------------
+------+-----+
| value|count|
+------+-----+
|word27|    2|
|word28|    1|
|word29|    1|
+------+-----+
```

___

終わり
