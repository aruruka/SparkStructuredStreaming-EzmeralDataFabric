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

...to be continued...
