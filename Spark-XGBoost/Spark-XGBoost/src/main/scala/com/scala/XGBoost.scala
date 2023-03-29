//package com.scala
//
//import ml.dmlc.xgboost4j.scala.spark.XGBoost
//import org.apache.spark.ml.feature.VectorAssembler
//import org.apache.spark.sql.{DataFrame, SparkSession}
//
///**
// * author     ：test-abc
// * date       ：Created in 2019/9/3 11:04
// * description：xgboost demo
// * modified By：
// */
//
//object XgboostDemo {
//  def main(args: Array[String]): Unit = {
//    val spark: SparkSession = SparkSession.builder()
//      .appName("SparkSql")
//            .master("local[2]")
//      .getOrCreate()
//    //准备示例数据，将数据转为dataframe
//    import spark.implicits._
//    val dataList: List[(Int, Double, Double, Double, Double, Double, Double)] = List(
//      (0, 8.9255, -6.7863, 11.9081, 5.093, 11.4607, -9.2834),
//      (1, 11.5006, -4.1473, 13.8588, 5.389, 12.3622, 7.0433),
//      (0, 8.6093, -2.7457, 12.0805, 7.8928, 10.5825, -9.0837),
//      (1, 11.0604, -2.1518, 8.9522, 7.1957, 12.5846, -1.8361),
//      (1, 9.8369, -1.4834, 12.8746, 6.6375, 12.2772, 2.4486),
//      (1, 11.4763, -2.3182, 12.608, 8.6264, 10.9621, 3.5609),
//      (0, 11.8091, -0.0832, 9.3494, 4.2916, 11.1355, -8.0198),
//      (0, 13.558, -7.9881, 13.8776, 7.5985, 8.6543, 0.831),
//      (0, 16.1071, 2.4426, 13.9307, 5.6327, 8.8014, 6.163),
//      (1, 12.5088, 1.9743, 8.896, 5.4508, 13.6043, -16.2859),
//      (0, 5.0702, -0.5447, 9.59, 4.2987, 12.391, -18.8687),
//      (0, 12.7188, -7.975, 10.3757, 9.0101, 12.857, -12.0852),
//      (0, 8.7671, -4.6154, 9.7242, 7.4242, 9.0254, 1.4247),
//      (1, 16.3699, 1.5934, 16.7395, 7.333, 12.145, 5.9004),
//      (0, 13.808, 5.0514, 17.2611, 8.512, 12.8517, -9.1622),
//      (0, 3.9416, 2.6562, 13.3633, 6.8895, 12.2806, -16.162),
//      (0, 5.0615, 0.2689, 15.1325, 3.6587, 13.5276, -6.5477),
//      (1, 8.4199, -1.8128, 8.1202, 5.3955, 9.7184, -17.839),
//      (0, 4.875, 1.2646, 11.919, 8.465, 10.7203, -0.6707),
//      (1, 4.409, -0.7863, 15.1828, 8.0631, 11.2831, -0.7356))
//
//    val inputDF: DataFrame = dataList.toDF("label", "feature1", "feature2", "feature3", "feature4", "feature5", "feature6")
//    //将需要转换的列合并为向量列
//    val transCols: Array[String] = Array("feature1", "feature2", "feature3", "feature4", "feature5", "feature6")
//    val assembler: VectorAssembler = new VectorAssembler().setInputCols(transCols).setOutputCol("features")
//    val xGBoostTrainInput: DataFrame = assembler.transform(inputDF).select("features","label")
//    xGBoostTrainInput.show(10)
//
//    //    val paramMap = List(
//    //      "eta" -> 0.01, //学习率
//    //      "gamma" -> 0.1, //用于控制是否后剪枝的参数,越大越保守，一般0.1、0.2这样子。
//    //      "lambda" -> 2, //控制模型复杂度的权重值的L2正则化项参数，参数越大，模型越不容易过拟合。
//    //      "subsample" -> 0.8, //随机采样训练样本
//    //      "colsample_bytree" -> 0.8, //生成树时进行的列采样
//    //      "max_depth" -> 5, //构建树的深度，越大越容易过拟合
//    //      "min_child_weight" -> 5,
//    //      "objective" -> "multi:softprob",  //定义学习任务及相应的学习目标
//    //      "eval_metric" -> "merror",
//    //      "num_class" -> 21
//    //    ).toMap
//
//    val paramMap = List(
//      "colsample_bytree" -> 1,
//      "eta" -> 0.05f, //就是学习率
//      "max_depth" -> 8, //树的最大深度
//      "min_child_weight" -> 5, //
//      "n_estimators" -> 120,
//      "subsample" -> 0.7
//    ).toMap
//
//
//    //模型训练
//    val xgBoostModel = XGBoost.trainWithDataFrame(xGBoostTrainInput, paramMap, round = 10, nWorkers =1,
//      useExternalMemory = true, featureCol = "features", labelCol = "label")
//
//    //准备预测数据
//    val testList: List[( Double, Double, Double, Double, Double, Double)] = List(
//      ( 8.9225, -6.7863, 11.9081, 5.093, 11.4607, -9.2834),
//      ( 11.5006, -4.1473, 13.8588, 5.389, 12.3622, 7.0433),
//      ( 8.6093, -2.7457, 12.0805, 7.8928, 10.5825, -9.0837),
//      ( 11.0604, -2.1518, 8.9522, 7.1957, 12.5846, -1.8361),
//      ( 9.8369, -11.4834, 12.8746, 6.6375, 12.2772, 2.4486),
//      ( 11.4763, -2.3182, 12.608, 8.6264, 10.9621, 3.5609),
//      ( 11.8091, -10.0832, 9.3494, 4.2916, 11.1355, -8.0198),
//      ( 13.558, -7.9881, 13.8776, 7.5985, 8.6543, 0.831),
//      ( 16.1071, 1.4426, 13.9307, 5.6327, 8.8014, 6.163),
//      ( 12.5088, 2.9743, 8.896, 5.4508, 13.6043, -16.2859),
//      ( 5.0702, -0.5447, 9.59, 4.2987, 12.391, -18.8687),
//      ( 12.7188, -7.975, 10.3757, 9.0101, 12.857, -12.0852),
//      ( 8.7671, -4.6154, 8.7242, 7.4242, 9.0254, 1.4247),
//      ( 16.3699, 1.5934, 16.7395, 7.333, 12.145, 5.9004),
//      ( 13.808, 5.0514, 17.2611, 8.512, 12.8517, -9.1622),
//      ( 3.9416, 2.6562, 13.3633, 6.8895, 12.2806, -16.162),
//      ( 5.0615, 0.2689, 15.1325, 3.6587, 13.5276, -6.5477),
//      ( 8.4199, -1.8128, 9.1202, 5.3955, 9.7184, -17.839),
//      ( 5.875, 1.2646, 11.919, 8.465, 10.7203, -0.6707),
//      ( 5.409, -0.7863, 15.1828, 8.0631, 11.2831, -0.7356))
//
//    val testDf: DataFrame = testList.toDF("feature1", "feature2", "feature3", "feature4", "feature5", "feature6")
//    //将测试数据集转为向量
//    val xGBoostTestInput: DataFrame = assembler.transform(testDf).select("features")
//    xGBoostTestInput.show(10)
//    //模型预测
//    val output: DataFrame = xgBoostModel.transform(xGBoostTestInput)
//    output.show()
////    spark.close()
//  }
//}
