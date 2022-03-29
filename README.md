#### 工程简介
基于github项目[mysql-binlog-connector-java](https://github.com/shyiko/mysql-binlog-connector-java)二次封装，
通过监听binlog日志，解决多模块项目冗余字段更新不及时，目前版本仅支持监听一个数据源下的一个数据库的多张表，所监听的数据库必须开启binlog日志。
#### pom文件
##### 引入依赖
```xml
<dependency>
      <groupId>io.github.heqizhengya</groupId>
      <artifactId>mysql-binlog-transform2db-java</artifactId>
      <version>v0.0.1-Alpha</version>
</dependency>
```  
##### 注意事项
该项目使用的数据库驱动版本为8.0.22，驱动名称为com.mysql.cj.jdbc.Driver，若和开发使用项目版本冲突，如下引入
```xml
<dependency>
    <groupId>io.github.heqizhengya</groupId>
    <artifactId>mysql-binlog-transform2db-java</artifactId>
    <version>v0.0.1-Alpha</version>
    <exclusions>
        <exclusion>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```
#### yml文件
##### 配置
```yaml
transform2db:
  # 默认跳过ext_delete_rows,ext_write_rows监听事件,如无修改,无需设置
  skip-event-types: ext_delete_rows,ext_write_rows
  # 默认监听线程数量为5
  consumer-threads: 5
  # 默认轮询处理休眠时间为1000ms
  queue-sleep: 1000
  # 默认关闭监听事件,需要手动开启
  open: true
  # 监听数据源配置
  datasource:
    host: localhost
    port: 3306
    username: *
    password: *
    # 监听指定数据库
    schema: test
    # 默认驱动名称为com.mysql.cj.jdbc.Driver,如不冲突,无需设置
    driver-name: com.mysql.jdbc.Driver
    # 指定表名称,支持多张表
    table:
      - test_prometheus
      - test_listener
```
##### 注意事项
该项目是基于数据库和表名称test-test_listener格式获取对应的注入对象，所以必须指定对应的数据库和表名称，否则启动报错java.lang.IllegalArgumentException: TableList is empty
```text
java.lang.IllegalStateException: Failed to execute CommandLineRunner
	at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:798) [spring-boot-2.3.7.RELEASE.jar:2.3.7.RELEASE]
	at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:779) [spring-boot-2.3.7.RELEASE.jar:2.3.7.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:322) [spring-boot-2.3.7.RELEASE.jar:2.3.7.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1237) [spring-boot-2.3.7.RELEASE.jar:2.3.7.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1226) [spring-boot-2.3.7.RELEASE.jar:2.3.7.RELEASE]
	at cn.sunpig.saas.SaasApplication.main(SaasApplication.java:14) [classes/:na]
Caused by: java.lang.IllegalArgumentException: TableList is empty
	at com.csdata.transform2db.listener.InitBinLogListener.run(InitBinLogListener.java:45) ~[mysql-binlog-transform2db-1.0.0-Alpha.jar:na]
	at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:795) [spring-boot-2.3.7.RELEASE.jar:2.3.7.RELEASE]
	... 5 common frames omitted
```
#### 启动配置
##### 设置包扫描
如不设置scanBasePackages = {"com.github.heqizheng.*"}，则依赖则无法正常使用
```java
@SpringBootApplication(scanBasePackages = {"com.github.heqizheng.*"})
public class SaasApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SaasApplication.class, args);
    }
    
}
```
##### 项目启动
有以下日志，项目启动成功
```text
2022-03-28 10:58:57.804  INFO 77495 --- [           main] c.c.t.listener.InitBinLogListener        : Initial properties：Transform2DBProperties{open=true, dataSource=DataSource{host='**.**.**.**', port=3306, username='**', password='**', schema='test', table=[test_prometheus], driverName='com.mysql.cj.jdbc.Driver'}, consumerThreads=5, queueSleep=1000, eventTypes=[EXT_DELETE_ROWS, EXT_WRITE_ROWS]}
2022-03-28 11:33:00.600  INFO 93891 --- [           main] c.c.t.listener.InitBinLogListener        : Transform2db start
2022-03-28 10:58:57.868  INFO 77495 --- [           main] c.c.t.listener.InitBinLogListener        : Register listening information, register schema：test, register table：test_prometheus
2022-03-28 10:58:58.468  INFO 77495 --- [           main] c.g.shyiko.mysql.binlog.BinaryLogClient  : Connected to **.**.**.**:3306 at binlog.000001/1687498 (sid:65535, cid:33935)
```
##### 注意事项
如不设置open，启动日志输出Transform2db not open，依赖未开启
#### 实现接口
每设置一张监听的表，就需要实现DefaultTransform2DBListener接口的类，@Service的名称需要为固定格式，@Service("数据库名称-监听的表的名称")代码如下
```java
@Slf4j
@Service("test-test_prometheus")
public class PrometheusListener implements DefaultTransform2DBListener {
    @Override
    public void transform2DB(BinLogItem item) {
        log.info("监听数据库发生变化：{}", JSONUtil.toJsonStr(item));
    }
}
```
##### 监听日志
修改数据库test_prometheus表数据，日志输出如下
```text
2022-03-28 11:28:59.373  INFO 91341 --- [pool-1-thread-2] c.s.s.service.impl.PrometheusListener    : 监听数据库发生变化：{"before":{"msg":"heqizheng","data":"何其正","id":4},"columns":{"msg":{"schema":"test","colName":"msg",
"dataType":"varchar","inx":2,"table":"test_prometheus"},"data":{"schema":"test","colName":"data","dataType":"varchar","inx":1,"table":"test_prometheus"},"id":{"schema":"test","colName":"id","dataType":"int","inx":0,
"table":"test_prometheus"}},"dbTable":"test-test_prometheus","eventType":"EXT_UPDATE_ROWS","after":{"msg":"何其正","data":"何其正","id":4}}
```
