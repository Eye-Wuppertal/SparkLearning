package com.scala

import ml.dmlc.xgboost4j.scala.spark.{XGBoostClassificationModel, XGBoostClassifier}//XGBoostClassifier
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.ml.linalg.{DenseVector, SparseVector, Vector, Vectors}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}


object XGBoost3 {
//  Logger.getLogger("org").setLevel(Level.ERROR)
//  Logger.getLogger("akka").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {

    val spark: SparkSession = SparkSession.builder.appName("xgb").master("local[2]").getOrCreate()
    val sc: SparkContext = spark.sparkContext
    import spark.implicits._
    val data: DataFrame = spark.read.option("inferSchema", true).option("header", true).csv("./Spark-XGBoost/src/main/resources/resource/iris.csv").toDF("x1", "x2", "x3", "x4","label")//sepal_length,sepal_width,petal_length,petal_width,IS_TARGET

    val vectorAssembler: VectorAssembler = new VectorAssembler().setInputCols(Array("x1", "x2", "x3", "x4")).setOutputCol("features")
    val train: DataFrame = vectorAssembler.transform(data)

    train.printSchema()
    train.show(10)
    println(train.count())

    val paramsMap = Map(
      ("eta" -> 0.1f), ("max_depth" -> 10), ("objective" -> "binary:logistic"), ("num_round" -> 10), ("num_works" -> 1)//, ("num_class" -> 2)
    )


    val xgb = new XGBoostClassifier(xgboostParams = paramsMap)//XGBoostEstimator  XGBoostClassifier
    xgb.setFeaturesCol("features")
    xgb.setLabelCol("label")

    val clf: XGBoostClassificationModel = xgb.fit(train)

    val trainPrediction: DataFrame = clf.transform(train)

    val scoreTrain: RDD[(Double, Double)] = trainPrediction.select($"prediction", $"label").rdd.map { row: Row =>
      val pred: Double = row.getDouble(0)
      val label: Double = row.getInt(1).toDouble
      (pred, label)
    }
    val trainMetric = new BinaryClassificationMetrics(scoreTrain)
    val trainAuc: Double = trainMetric.areaUnderROC()

    println("@@@ xgb score info : \n" + "train AUC :" + trainAuc)


  }

}
