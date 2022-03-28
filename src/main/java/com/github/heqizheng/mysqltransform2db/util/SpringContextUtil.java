package com.github.heqizheng.mysqltransform2db.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author heqizheng
 * @version 1.0
 * @date 2022/3/25
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext context = null;
    private static SpringContextUtil stools = null;

    public synchronized static SpringContextUtil init() {
        if (stools == null) {
            stools = new SpringContextUtil();
        }
        return stools;
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        context = applicationContext;
    }

    public synchronized static Object getBean(String beanName) {
        return context.getBean(beanName);
    }
}
