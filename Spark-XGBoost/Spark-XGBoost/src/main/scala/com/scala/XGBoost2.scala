//package com.scala
//import org.apache.log4j.{Level, Logger}
//import ml.dmlc.xgboost4j.java.{DMatrix, XGBoost}
//import ml.dmlc.xgboost4j.scala.spark.{XGBoostClassificationModel, XGBoostClassifier}
//import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
//import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorAssembler}
//import org.apache.spark.ml.linalg.Vectors
//import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
//import org.apache.spark.ml.{Pipeline, PipelineModel}
//import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
//import org.apache.spark.sql.{DataFrame, Row, SparkSession}
//
//import scala.collection.mutable.ArrayBuffer
////import ml.dmlc.xgboost4j.scala.{DMatrix, XGBoost}
//
//
//object ScXGBoostModelDebug {
//  def main(args: Array[String]): Unit = {
//      Logger.getLogger("org").setLevel(Level.ERROR)
//      Logger.getLogger("akka").setLevel(Level.ERROR)
//
//    //    val conf = new SparkConf().setAppName("scXgb").setMaster("local")
//    //    val sc = new SparkContext(conf)
//    val spark = SparkSession.builder.master("local[2]").appName("tryXGB").getOrCreate()
//
////    val spark = SparkSession.builder()
////      .master("local")
////      .appName("xgboost_spark_demo")
////      //      .config("spark.memory.fraction", 0.3)
//      //      .config("spark.shuffle.memoryFraction", 0.5)
////      .getOrCreate()
//
//
//
//    val basePath = "./src/main/resources/"
//    //1： 加载并解析数据
////    val df_train = spark.read.csv(".//adult_train.csv")
//    val df_train = spark.read.format("com.databricks.spark.csv")
//      .option("header", "false")
//      .option("inferSchema", true.toString)
//      .load(basePath + "/resource/adult_train.csv")
////      .load("/adult_train.csv")
////      .load("D:\\adult_train.csv")
//
//    val xgbTrain = processData(df_train)
//
////    val df_train = spark.read.csv(".//adult_test.csv")
//    val df_test = spark.read.format("com.databricks.spark.csv")
//      .option("header", "false")
//      .option("inferSchema", true.toString)
//      .load(basePath + "/resource/adult_train.csv")
////      .load("/adult_train.csv")
////      .load("D:\\adult_test.csv")
//
//    val xgbTest = processData(df_test)
//
//    val xgbParam = Map(
//      "eta" -> 0.1f,
//      "max_depth" -> 5,
//      //      "objective" -> "multi:softprob",
//      "objective" -> "binary:logistic",
//
//      //      "num_class" -> 2,  //二分模型不设置
////      "num_round" -> 2,
//            "num_workers" -> 2
//      //      "tree_method" -> treeMethod,
//      //      "eval_sets" -> Map("eval1" -> eval1, "eval2" -> eval2)
//    )
//
//    //    val xgbClassifier = new XGBoostClassifier(xgbParam)
//    val booster = new XGBoostClassifier(xgbParam)
//      .setEvalMetric("auc")
//      .setMaxDepth(5)
//    //        .setFeaturesCol("features")
//    //        .setLabelCol("class")
//
////        val xgbClassificationModel = xgbClassifier.fit(xgbTrain)
//    //    val results = xgbClassificationModel.transform(xgbTest)
////    xgbClassificationModel.save("./sc_xgb.bin")
//    //    xgbClassificationModel.write.overwrite().save("~/Desktop/sc_xgb_mdoel")
//    //    results.show()
//
//
//    val assembler = new VectorAssembler()
//      .setInputCols(Array("_c0", "_c2","_c4","_c10","_c11","_c12"))
//      .setOutputCol("features")
//
//    val labelIndexer = new StringIndexer()
//      .setInputCol("_c14")
//      .setOutputCol("classIndex")
//      .fit(df_train)
//
//    booster.setFeaturesCol("features")
//    booster.setLabelCol("classIndex")
//    val labelConverter = new IndexToString()
//      .setInputCol("prediction")
//      .setOutputCol("realLabel")
//      .setLabels(labelIndexer.labels)
//
//    val pipeline = new Pipeline()
//      .setStages(Array(assembler, labelIndexer, booster, labelConverter))
//    val model = pipeline.fit(df_train)
//
//    // Batch prediction
//    val prediction = model.transform(df_test)
//    prediction.show(false)
//
//    // Model evaluation
//    val evaluator = new MulticlassClassificationEvaluator()
//    evaluator.setLabelCol("classIndex")
//    evaluator.setPredictionCol("prediction")
//    val accuracy = evaluator.evaluate(prediction)
//    println("The model accuracy is : " + accuracy)
//
//    // Tune model using cross validation
//    val paramGrid = new ParamGridBuilder()
//      .addGrid(booster.maxDepth, Array(3, 4))
//      .addGrid(booster.eta, Array(0.2, 0.3))
//      .build()
//    val cv = new CrossValidator() //使用交叉验证训练模型
//      .setEstimator(pipeline)
//      .setEvaluator(evaluator)
//      .setEstimatorParamMaps(paramGrid)
//      .setNumFolds(3)
//
//    val cvModel = cv.fit(df_train)
//
//    val inputPath = "./inputPath"
//    //    val nativeModelPath = "./nativeModelPath"
//    val nativeModelPath = "./xgb.best.bin"
//
//    val pipelineModelPath = "./pipelineModelPath"
//
//    val bestModel = cvModel.bestModel.asInstanceOf[PipelineModel].stages(2)
//      .asInstanceOf[XGBoostClassificationModel]
//    println("The params of best XGBoostClassification model : " +
//      bestModel.extractParamMap())
//    println("The training summary of best XGBoostClassificationModel : " +
//      bestModel.summary)
//
//    // Export the XGBoostClassificationModel as local XGBoost model,
//    // then you can load it back in local Python environment.
//    bestModel.nativeBooster.saveModel(nativeModelPath)
//
//    // ML pipeline persistence
//    //    model.write.overwrite().save(pipelineModelPath)
//
//    // Load a saved model and serving
//    //    val model2 = PipelineModel.load(pipelineModelPath)
//    //    model2.transform(df_test).show(false)
//
//    println("\n\nxgb model inference test data:  ")
//    //bestModel.transform(xgbTest).show(false)
//
//    val res = bestModel.transform(xgbTest)
//    res.show(false)
//
//    //res.write.mode("overwrite").option("header","ture").option("encoding","utf-8").csv("./scoreLabel")
//    //    res.repartition(1).write.option("header", "true").mode("overwrite")
//    //      .csv("./scoreLabel")
//
//    val scoreAndLabels = res.select(bestModel.getProbabilityCol, bestModel.getLabelCol).rdd
//      .map {row => {
//        val pro = row.getAs[org.apache.spark.ml.linalg.DenseVector](0)
//        val label = row.getAs[Double](1)
//        val score = pro(1)
//        (score, label)}}
//    val metric = new BinaryClassificationMetrics(scoreAndLabels)
//
//    val auc = metric.areaUnderROC()
//    val pr = metric.areaUnderPR()
//
//
//    val scoreAndLabels2 = res.select(bestModel.getPredictionCol, bestModel.getLabelCol).rdd
//      .map {case Row(predLabel: Double, label: Double) => (predLabel, label)}
//    val metric2 = new BinaryClassificationMetrics(scoreAndLabels2)
//
//    val precision = metric2.precisionByThreshold
//    val recall = metric2.recallByThreshold()
//    val f1score = metric2.fMeasureByThreshold()
//    val pr2 = metric2.areaUnderPR()
//    val prc = metric2.pr()
//    println("\n\n auc:  " + auc)
//    println("pr: ",pr)
//    println("presion: ", precision.toJavaRDD().collect())
//    println("recall: ", recall.toJavaRDD().collect())
//    println("f1score: ", f1score.toJavaRDD().collect())
//    println("pr2: ", pr2)
//    println("prc:  ", prc.toJavaRDD().collect())
//
//
//    println("\n\n2222***********xgb model inference test data second:  ")
//    //    val xgbModel = PipelineModel.load(nativeModelPath)
//    val booster2 = XGBoost.loadModel(nativeModelPath) //加载模型
//
//    val matrix1 = new Array[Float](2 * 6)
//    matrix1(0) = 27.0f;
//    matrix1(1) = 301514.0f;
//    matrix1(2) = 10.0f;
//    matrix1(3) = 0.0f;
//    matrix1(4) = 0.0f;
//    matrix1(5) = 40.0f;
//
//    matrix1(6) = 41.0f;
//    matrix1(7) = 219155.0f;
//    matrix1(8) = 4.0f;
//    matrix1(9) = 0.0f;
//    matrix1(10) = 0.0f;
//    matrix1(11) = 40.0f;
//
//    val dMatrix = new DMatrix(matrix1, 2, 6, -9999)
//    val predicts = booster2.predict(dMatrix) //预测模型
//    println(predicts.toString)
//
//    predicts.foreach(
//      pred => pred.foreach(
//        x => println("pred:  " + x)
//      )
//    )
//
//    //    pred:  0.12153101
//    //    pred:  0.051592674   离线训练和线上一致
//
//  }
//
//
//  def processData(df_data: DataFrame): DataFrame = { //数据处理函数
//    var columns = df_data.columns.clone()
//    var feature_columns = new ArrayBuffer[String]()
//    //    for (i <- 0 until columns.length - 1) {
//    //      println(i + "  ***************:   " + columns(i).toString)
//    //      feature_columns += columns(i)
//    //    }
//    feature_columns += "_c0"
//    feature_columns += "_c2"
//    feature_columns += "_c4"
//
//    feature_columns += "_c10"
//    feature_columns += "_c11"
//    feature_columns += "_c12"
//
//
//    val stringIndexer = new StringIndexer()
//      .setInputCol("_c14")
//      .setOutputCol("classIndex")
//      .fit(df_data)
//
//    val labelTransformed = stringIndexer.transform(df_data).drop("_c14")
//
//    val train_vectorAssembler = new VectorAssembler()
//      .setInputCols(feature_columns.toArray)
//      .setOutputCol("features")
//
//    val xgbData = train_vectorAssembler.transform(labelTransformed).select("features", "classIndex")
//    return xgbData
//  }
//}
