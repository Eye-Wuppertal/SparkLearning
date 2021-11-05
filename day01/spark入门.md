# Spark 基础知识

在hadoop上替换MapReduce，速度快100+倍

## 组成模块

![1609550802202](.\img\1609550802202.png)

# Spark环境搭建

## 1. Local本地模式

### 原理

![1609551528270](.\img\1609551528270.png)

### 安装spark到linux虚拟机上

![image-20211012190738947](.\img\image-20211012190738947.png)

```shell
tar -zxvf /data/packs/spark-3.0.1-bin-hadoop2.7.tgz -C /software/    # 解压
```

![image-20211012191545651](.\img\image-20211012191545651.png)

```shell
mv /software/spark-3.0.1-bin-hadoop2.7/ /software/spark/    #修改路径名
```

![image-20211012192544055](.\img\image-20211012192544055.png)

```shell
# 需要root 权限
chown -R root /software/spark
chgrp -R root /software/spark
```

### 配置环境变量

```shell
vi /etc/profile

# Spark
export SPARK_HOME=/software/spark
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOMR/sbin
```

### 测试

```shell
/software/spark/bin/spark-shell 
```

![image-20211012193532241](.\img\image-20211012193532241.png)

打开web  UI http://master:4040

![image-20211013144114728](.\img\image-20211013144114728.png)

### 跑例程                                                                                                                                                                                                                                                                                                                                                                                                                                                       

新建一个txt文本

```shell
vi /root/words.txt

ultraman tero ultraman ace ultraman haha
haha ultraman jack
hi jack hi ross
```

执行wordcount

```scala
val textFile = sc.textFile("file:///root/words.txt")
val counts = textFile.flatMap(_.split(" ")).map((_,1)).reduceByKey(_ + _)
counts.collect
```

![image-20211012203807365](.\img\image-20211012203807365.png)

## 2. Standalone 独立集群

### 原理

![1609553123267](.\img\1609553123267.png)

### 搭建步骤

```shell

# 在cd /software/spark/conf下 
mv slaves.template slaves
vi slaves

slave1
slave2

mv spark-env.sh.template  spark-env.sh
vi spark-env.sh 

## 设置JAVA安装目录
JAVA_HOME=/software/java

## HADOOP软件配置文件目录，读取HDFS上文件和运行Spark在YARN集群时需要,先提前配上
HADOOP_CONF_DIR=/software/hadoop/etc/hadoop
YARN_CONF_DIR=/software/hadoop/etc/hadoop

## 指定spark老大Master的IP和提交任务的通信端口
SPARK_MASTER_HOST=master
SPARK_MASTER_PORT=7077
SPARK_MASTER_WEBUI_PORT=8080
SPARK_WORKER_CORES=1
SPARK_WORKER_MEMORY=1g

# 将安装的spark克隆到其他两个节点,profile 文件也一起拷贝
scp -r /software/spark/ slave1:/software/
scp -r /software/spark/ slave2:/software/
scp /etc/profile slave1:/etc/
scp /etc/profile slave2:/etc/
. /etc/profile	

```

### 测试

```shell
# 启动集群
/software/spark/sbin/start-all.sh
```

![image-20211012212209392](.\img\image-20211012212209392.png)

http://master:8080/

(当时配置有一丢丢小问题，图里的worker应该是2)

![image-20211013143620720](.\img\image-20211013143620720.png)

```shell
# 启动
/software/spark/bin/spark-shell --master spark://master:7077
```

![image-20211013153439635](.\img\image-20211013153439635.png)

### 跑wordcount

```shell
# 上传文件到hdfs方便worker读取
hadoop fs -mkdir -p /wordcount/input #创建目录 
hadoop fs -put /root/words.txt /wordcount/input
```

```scala
val textFile = sc.textFile("hdfs://master:9000/data/wordcount/input/words.txt")
val counts = textFile.flatMap(_.split(" ")).map((_,1)).reduceByKey(_ + _)
counts.collect
counts.saveAsTextFile("hdfs://master:9000/data/wordcount/output2")
```

http://master:9870/explorer.html#/data/wordcount/output2

![image-20211013162449297](.\img\image-20211013162449297.png)

http://master:4040/jobs/

![image-20211013162507111](.\img\image-20211013162507111.png)



