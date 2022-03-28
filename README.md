# 工程简介
基于github项目[mysql-binlog-connector-java](https://github.com/shyiko/mysql-binlog-connector-java)二次封装，
通过监听binlog日志，解决多模块项目冗余字段更新不及时，目前版本仅支持监听一个数据源下的一个数据库的多张表，所监听的数据库必须开启binlog日志。
# 项目配置
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
    username: root
    password: root
    # 监听指定数据库
    schema: test
    # 默认驱动名称为com.mysql.cj.jdbc.Driver,如不冲突,无需设置
    driver-name: com.mysql.jdbc.Driver
    # 指定表名称,支持多张表
    table:
      - test_prometheus
      - test_listener
```


