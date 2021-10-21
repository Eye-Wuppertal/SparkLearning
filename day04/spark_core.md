# RDD详解

## why?

![1609637735554](.\img\1609637735554.png)

## what?

A Resilient Distributed Dataset (RDD), the basic abstraction in Spark. 

RDD:弹性分布式数据集,是Spark中最基本的数据抽象,用来表示分布式集合,支持分布式操作!



## 五大属性

Internally, each RDD is characterized by five main properties:

 - 分区列表: A list of partitions

 - 计算函数: A function for computing each split

 - 依赖关系: A list of dependencies on other RDDs

 - 分区器: Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned)

 - 计算位置：Optionally, a list of preferred locations to compute each split on (e.g. block locations for
   an HDFS file)

   

## WordCount中的RDD的五大属性

![1609638705317](.\img\1609638705317.png)





# RDD的创建

RDD中的数据可以来源于2个地方：本地集合或外部数据源

![1609640251337](.\img\1609640251337.png)



```java 
package cn.itcast.core

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Author itcast
 * Desc 演示RDD的创建
 */
object RDDDemo01_Create {
  def main(args: Array[String]): Unit = {
    //TODO 0.env/创建环境
    val conf: SparkConf = new SparkConf().setAppName("spark").setMaster("local[*]")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //TODO 1.source/加载数据/创建RDD
    val rdd1: RDD[Int] = sc.parallelize(1 to 10) //8
    val rdd2: RDD[Int] = sc.parallelize(1 to 10,3) //3

    val rdd3: RDD[Int] = sc.makeRDD(1 to 10)//底层是parallelize //8
    val rdd4: RDD[Int] = sc.makeRDD(1 to 10,4) //4

    //RDD[一行行的数据]
    val rdd5: RDD[String] = sc.textFile("data/input/words.txt")//2
    val rdd6: RDD[String] = sc.textFile("data/input/words.txt",3)//3
    //RDD[一行行的数据]
    val rdd7: RDD[String] = sc.textFile("data/input/ratings10")//10
    val rdd8: RDD[String] = sc.textFile("data/input/ratings10",3)//10
    //RDD[(文件名, 一行行的数据),(文件名, 一行行的数据)....]
    val rdd9: RDD[(String, String)] = sc.wholeTextFiles("data/input/ratings10")//2
    val rdd10: RDD[(String, String)] = sc.wholeTextFiles("data/input/ratings10",3)//3

    println(rdd1.getNumPartitions)//8 //底层partitions.length
    println(rdd2.partitions.length)//3
    println(rdd3.getNumPartitions)//8
    println(rdd4.getNumPartitions)//4
    println(rdd5.getNumPartitions)//2
    println(rdd6.getNumPartitions)//3
    println(rdd7.getNumPartitions)//10
    println(rdd8.getNumPartitions)//10
    println(rdd9.getNumPartitions)//2
    println(rdd10.getNumPartitions)//3

    //TODO 2.transformation
    //TODO 3.sink/输出
  }
}

```





# RDD操作

## 分类

![1609641960937](F:\SparkLearningFile\spark3.0\代码-笔记\Spark-day02\笔记\Spark-day02.assets\1609641960937.png)



## 基本算子/操作/方法/API

map

faltMap

filter

foreach

saveAsTextFile

```Java
package cn.itcast.core

import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Author itcast
 * Desc 演示RDD的基本操作
 */
object RDDDemo02_Basic {
  def main(args: Array[String]): Unit = {
    //TODO 0.env/创建环境
    val conf: SparkConf = new SparkConf().setAppName("spark").setMaster("local[*]")
    val sc: SparkContext = new SparkContext(conf)
    sc.setLogLevel("WARN")

    //TODO 1.source/加载数据/创建RDD
    val lines: RDD[String] = sc.textFile("data/input/words.txt") //2

    //TODO 2.transformation
    val result: RDD[(String, Int)] = lines.filter(StringUtils.isNoneBlank(_))
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)

    //TODO 3.sink/输出/action
    result.foreach(println)
    result.saveAsTextFile("data/output/result4")
  }
}

```

