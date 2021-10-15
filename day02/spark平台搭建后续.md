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
val textFile = sc.textFile("hdfs://master:8020/wordcount/input/words.txt")
val counts = textFile.flatMap(_.split(" ")).map((_,1)).reduceByKey(_ + _)
counts.collect
counts.saveAsTextFile("hdfs://master:8020/wordcount/output2")
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

