package com.tal.core

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/*
    author: Tal
    TODO: 
*/

object RDD01_create {
  def main(args: Array[String]): Unit = {
    //TODO 0.env/创建环境
    val conf: SparkConf = new SparkConf().setAppName("spark").setMaster("local[*]")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //TODO 1.source/加载数据/创建RDD
    val rdd1: RDD[Int] = sc.parallelize(1 to 10)       //6
    val rdd2: RDD[Int] = sc.parallelize(1 to 10, 3)   //3

    val rdd3: RDD[Int] = sc.makeRDD(1 to 10)           //6
    val rdd4: RDD[Int] = sc.makeRDD(1 to 10, 4)   //4

    val rdd5: RDD[String] = sc.textFile("data/input/heart.csv")    //2
    val rdd6: RDD[String] = sc.textFile("data/input/heart.csv",5)    //5

    val rdd7: RDD[String] = sc.textFile("data/input")   //3
    val rdd8: RDD[String] = sc.textFile("data/input",5)   //6

    //wholeTextFiles(本地/HDFS文件夹,分区数)


    //TODO 2.transformation/数据操作/转换

    //TODO 3.sink/输出
    println(rdd1.getNumPartitions)
    println(rdd1.partitions.length)
    println(rdd2.getNumPartitions)
    println(rdd3.partitions.length)
    println(rdd4.getNumPartitions)
    println(rdd5.partitions.length)
    println(rdd6.getNumPartitions)
    println(rdd7.partitions.length)
    println(rdd8.getNumPartitions)

    //TODO 4.关闭资源
    sc.stop()

  }

}
