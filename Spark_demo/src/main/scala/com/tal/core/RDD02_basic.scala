package com.tal.core

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/*
    author: Tal
    TODO: 演示RDD的基本操作
*/

object RDD02_basic {
  def main(args: Array[String]): Unit = {
    //TODO 0.env/创建环境
    val conf: SparkConf = new SparkConf().setAppName("spark").setMaster("local[*]")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //TODO 1.source/加载数据/创建RDD
    val lines: RDD[String] = sc.textFile("data/input/words.txt")
    lines.filter(!_.isEmpty).flatMap(_.split(" "))

    //TODO 2.transformation/数据操作/转换

    //TODO 3.sink/输出

    //TODO 4.关闭资源

  }

}
