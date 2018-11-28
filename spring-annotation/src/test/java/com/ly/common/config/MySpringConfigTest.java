package com.ly.common.config;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.Assert.*;

/**
 * @author Administrator
 * @date 2018/11/27 17:34
 */
public class MySpringConfigTest {

    @Test
    public void testMySpring() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(MySpringConfig.class);
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();

        for (String beanName :
                beanDefinitionNames) {
            System.out.println("beanName = " + beanName);
        }

        Object user002 = applicationContext.getBean("user002");
        Object user001 = applicationContext.getBean("user001");
        System.out.println("user001 = " + user001);
        System.out.println("user002 = " + user002);
    }

}