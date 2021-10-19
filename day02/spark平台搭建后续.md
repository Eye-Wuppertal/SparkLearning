# Spark环境搭建

## 3. Standalone-HA

### 原理

![1609555149171](.\img\1609555149171.png)

### zookeeper安装配置

![image-20211014124912063](.\img\image-20211014124912063.png)

```shell
tar -zxvf /data/packas/apache-zookeeper-3.7.0-bin.tar.gz -C /software/
mv /software/apache-zookeeper-3.7.0-bin/ /software/zookeeper/
```

```shell
# 目前为止所学的全部环境变量
vi /etc/profile

# java and hadoop
export JAVA_HOME=/software/java
export HADOOP_HOME=/software/hadoop
export PATH=$PATH:$HADOOP_HOME/bin:$JAVA_HOME/bin:$HADOOP_HOME/sbin:$JAVA_HOME/sbin
export CLASSPATH=.:$JAVA_HOMR/lib/dt.jar:$JAVA_HOME/lib/tools.jar
# Spark
export SPARK_HOME=/software/spark
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOMR/sbin
# Zookeeper
export ZOOKEEPER_HOME=/software/zookeeper
export PATH=$PATH:$ZOOKEEPER_HOME/bin

. /etc/profile

# 在zookeeper的各个节点下 创建数据和日志目录
cd /software/zookeeper
mkdir data
mkdir logs

# 将zookeeper/conf目录下的zoo_sample.cfg文件拷贝一份，命名为zoo.cfg
cd /software/zookeeper/conf

cp zoo_sample.cfg zoo.cfg
# 修改zoo.cfg 配置文件
vi zoo.cfg

clientPort=2181
dataDir=/software/zookeeper/data
dataLogDir=/software/zookeeper/logs
server.1=master:2888:3888
server.2=slave1:2888:3888
server.3=slave2:2888:3888

cd /software/zookeeper/data
vi myid
1 # 服务器编号、

# 发送到其他两台机器
scp -r /software/zookeeper/ slave1:/software/
scp /etc/profile slave1:/etc
. /etc/profile

# 分别修改slave1和slave2的myid中的服务器编号
# 启动
zkServer.sh start
```

![image-20211014145111044](.\img\image-20211014145111044.png)

### 修改 spark配置

```shell
vi /software/spark/conf/spark-env.sh
# 注释掉 SPARK_MASTER_HOST=master
# 增加 
SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=master:2181,slave1:2181,slave2:2181 -Dspark.deploy.zookeeper.dir=/spark-ha"

# 复制配置
scp -r /software/spark/conf/spark-env.sh slave1:/software/spark/conf
```

### 测试

1. 启动zk

   ```shell
   zkServer.sh status
   zkServer.sh stop
   zkServer.sh start

   ```

   ![image-20211014150521555](.\img\image-20211014150521555.png)

2. master启动spark集群

```shell
/software/spark/sbin/start-all.sh
```

![image-20211014151731299](.\img\image-20211014151731299.png)

3. slave1上单独挂个master

```shell
/software/spark/sbin/start-master.sh
```

![image-20211014151838298](.\img\image-20211014151838298.png)

4. http://master:8080/

   ![image-20211014152457931](.\img\image-20211014152457931.png)

http://slave1:8080/![image-20211014152426227](.\img\image-20211014152426227.png)

5. 模拟master故障

![image-20211014152729602](.\img\image-20211014152729602.png)

再次查看

![image-20211015004206495](.\img\image-20211015004206495.png)

![image-20211015004132196](.\img\image-20211015004132196.png)

测试WordCount

```shell
/software/spark/bin/spark-shell --master spark://master:7077,slave1:7077
```

```scala
val textFile = sc.textFile("hdfs://master:9000/wordcount/input/words.txt")
val counts = textFile.flatMap(_.split(" ")).map((_,1)).reduceByKey(_ + _)
counts.collect
counts.saveAsTextFile("hdfs://master:9000/wordcount/output2")
```



### 中途出现的问题

8080界面打不开

![image-20211014235541560](.\img\image-20211014235541560.png)

检查端口占用情况

![image-20211014235628989](.\img\image-20211014235628989.png)

可以看到8080端口确实是被zookeeper占用

```shell
zkServer.sh start-foreground # 查看启动日志
```

![img](.\img\e5a4476f298ae4d1ce25d9b870d34959.png)

发现是一个叫AdminServer的东西在使用8080端口，一个运行在8080端口的嵌入式Jetty服务，为一些命令提供了HTTP接口

```shell
 vi /software/zookeeper/conf/zoo.cfg
 # 进入zoo.cfg中，修改端口
 admin.serverPort=8060
 #解决
```

## 4. Spark On Yarn

原理

![1609556988870](.\img\1609556988870.png)

### 准备工作

关闭之前的Spark-Standalone集群

```shell
 /software/spark/sbin/stop-all.sh
```

### 1.配置Yarn历史服务器并关闭资源检查

```shell
vi /software/hadoop/etc/hadoop/yarn-site.xml
```

```xml
<configuration>
    <!-- 配置yarn主节点的位置 -->
    <property>
        <name>yarn.resourcemanager.hostname</name>
        <value>master</value>
    </property>
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <!-- 设置yarn集群的内存分配方案 -->
    <property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>20480</value>
    </property>
    <property>
        <name>yarn.scheduler.minimum-allocation-mb</name>
        <value>2048</value>
    </property>
    <property>
        <name>yarn.nodemanager.vmem-pmem-ratio</name>
        <value>2.1</value>
    </property>
    <!-- 开启日志聚合功能 -->
    <property>
        <name>yarn.log-aggregation-enable</name>
        <value>true</value>
    </property>
    <!-- 设置聚合日志在hdfs上的保存时间 -->
    <property>
        <name>yarn.log-aggregation.retain-seconds</name>
        <value>604800</value>
    </property>
    <!-- 设置yarn历史服务器地址 -->
    <property>
        <name>yarn.log.server.url</name>
        <value>http://master:19888/jobhistory/logs</value>
    </property>
    <!-- 关闭yarn内存检查 -->
    <property>
        <name>yarn.nodemanager.pmem-check-enabled</name>
        <value>false</value>
    </property>
    <property>
        <name>yarn.nodemanager.vmem-check-enabled</name>
        <value>false</value>
    </property>
</configuration>
```

复制配置到其他节点

```shell
cd /software/hadoop/etc/hadoop
scp -r yarn-site.xml root@slave1:$PWD
scp -r yarn-site.xml root@slave2:$PWD
```

### 2.配置Spark的历史服务器和Yarn的整合

```shell
cd /software/spark/conf

mv spark-defaults.conf.template spark-defaults.conf
vi spark-defaults.conf
# 添加以下内容
spark.eventLog.enabled                  true
spark.eventLog.dir                      hdfs://master:9000/sparklog/
spark.eventLog.compress                 true
spark.yarn.historyServer.address        master:18080

vi /software/spark/conf/spark-env.sh
# 添加以下内容
## 配置spark历史日志存储地址
SPARK_HISTORY_OPTS="-Dspark.history.fs.logDirectory=hdfs://master:9000/sparklog/ -Dspark.history.fs.cleaner.enabled=true"

# sparklog需要手动创建
hadoop fs -mkdir -p /sparklog
# （创建目录失败，可能是安全模式导致的，关闭安全模式即可）
hadoop  dfsadmin -safemode leave

# 修改日志级别
cd /software/spark/conf
mv log4j.properties.template log4j.properties
vi log4j.properties
```

![image-20211017143226256](.\img\image-20211017143226256.png)

```shell
# 分发
cd /software/spark/conf
scp -r spark-env.sh root@slave1:$PWD
scp -r spark-env.sh root@slave2:$PWD

scp -r spark-defaults.conf root@slave1:$PWD
scp -r spark-defaults.conf root@slave2:$PWD

scp -r log4j.properties root@slave1:$PWD
scp -r log4j.properties root@slave2:$PWD
```

### 3.配置依赖的Spark 的jar包

```shell
# 1. 在HDFS上创建存储spark相关jar包的目录
hadoop fs -mkdir -p /spark/jars/

# 2. 上传$SPARK_HOME/jars所有jar包到HDFS
hadoop fs -put /software/spark/jars/* /spark/jars/

# 3. 在master上修改spark-defaults.conf
vi /software/spark/conf/spark-defaults.conf
# 添加
spark.yarn.jars  hdfs://master:9000/spark/jars/*
# 分发
cd /software/spark/conf
scp -r spark-defaults.conf root@slave1:$PWD
scp -r spark-defaults.conf root@slave2:$PWD
```

### 4.启动服务

```shell
# 在master上启动Hadoop集群
start-all.sh
# 在master上启动MRHistoryServer服务
mr-jobhistory-daemon.sh start historyserver
# 在master上启动SparkHistoryServer服务
/software/spark/sbin/start-history-server.sh
```

\- MRHistoryServer服务WEB UI页面：

http://master:19888

![image-20211017161110480](.\img\image-20211017161110480.png)

\- Spark HistoryServer服务WEB UI页面：

http://master:18080/

![image-20211017161524462](.\img\image-20211017161524462.png)

## 两种模式

### client-了解

![1609558928933](.\img\1609558928933.png)

### cluster模式-开发使用

![1609559145839](.\img\1609559145839.png)

## 操作

1.需要Yarn集群

2.历史服务器

3.提交任务的的客户端工具-spark-submit命令

4.待提交的spark任务/程序的字节码--可以使用示例程序



### client模式

```shell
SPARK_HOME=/software/spark
${SPARK_HOME}/bin/spark-submit \
--master yarn  \
--deploy-mode client \
--driver-memory 512m \
--driver-cores 1 \
--executor-memory 512m \
--num-executors 2 \
--executor-cores 1 \
--class org.apache.spark.examples.SparkPi \
${SPARK_HOME}/examples/jars/spark-examples_2.12-3.0.1.jar \
10
```

![image-20211019161608412](.\img\image-20211019161608412.png)

查看web界面

http://master:8088/cluster

![image-20211019161757223](.\img\image-20211019161757223.png)



### cluster模式

```shell
SPARK_HOME=/export/server/spark
${SPARK_HOME}/bin/spark-submit \
--master yarn \
--deploy-mode cluster \
--driver-memory 512m \
--executor-memory 512m \
--num-executors 1 \
--class org.apache.spark.examples.SparkPi \
${SPARK_HOME}/examples/jars/spark-examples_2.12-3.0.1.jar \
10
```

![image-20211019161904076](.\img\image-20211019161904076.png)

查看web界面

http://master:8088/cluster

![image-20211019162140264](.\img\image-20211019162140264.png)

![image-20211019162101090](.\img\image-20211019162101090.png)









## 
