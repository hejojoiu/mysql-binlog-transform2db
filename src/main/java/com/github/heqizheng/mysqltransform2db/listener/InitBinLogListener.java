package com.github.heqizheng.mysqltransform2db.listener;

import com.github.heqizheng.mysqltransform2db.configure.Transform2DBProperties;
import com.github.heqizheng.mysqltransform2db.util.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 *  SpringBoot启动成功后的执行业务线程操作
 *  CommandLineRunner去实现此操作
 *  在有多个可被执行的业务时，通过使用 @Order 注解，设置各个线程的启动顺序（value值由小到大表示启动顺序）。
 *  多个实现CommandLineRunner接口的类必须要设置启动顺序，不让程序启动会报错！
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
@Component
@Order(value = 1)
public class InitBinLogListener implements CommandLineRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(InitBinLogListener.class);

    @Resource
    Transform2DBProperties dbProperties;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Initial properties：{}", dbProperties.toString());
        if (!dbProperties.isOpen()) {
            LOGGER.info("Transform2db not open");
        } else {
            LOGGER.info("Transform2db start");
            Transform2DBProperties.DataSource dataSource = dbProperties.getDataSource();
            // 初始化监听器
            MySQLBinLogListener listener = new MySQLBinLogListener(dbProperties);
            // 获取table集合
            List<String> tableList = dataSource.getTable();
            if (CollectionUtil.isEmpty(tableList))
                throw new IllegalArgumentException("TableList is empty");
            // 注册监听
            tableList.forEach(table -> {
                LOGGER.info("Register listening information, register schema：" + dataSource.getSchema() + ", register table：" + table);
                try {
                    listener.regListener(dataSource, table, item -> {
                        LOGGER.info("Listening logic processing");
                    });
                } catch (Exception e) {
                    LOGGER.error("BinLog monitor exception：" + e);
                }
            });
            // 多线程消费
            listener.parse(dbProperties.getConsumerThreads(), dbProperties.getQueueSleep());
        }
    }
}
