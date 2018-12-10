package com.ly.cache.filter;

import com.ly.common.config.MySpringConfigJedis;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Administrator
 * @date 2018/12/10 15:21
 */
public class BloomFilterTest {

    @Test
    public void test() {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MySpringConfigJedis.class);
        BloomFilter filter = (BloomFilter) applicationContext.getBean("bloomFilter");
        boolean exist = filter.isExist("aaa", "aaa");
        System.out.println("exist = " + exist);
        boolean exist1 = filter.isExist("aaa", "aaa");
        System.out.println("exist1 = " + exist1);
        boolean exist2 = filter.isExist("aaa", "bbb");
        System.out.println("exist2 = " + exist2);
        boolean exist3 = filter.isExist("bbb", "aaa");
        System.out.println("exist3 = " + exist3);
        boolean exist4 = filter.isExist("ccc", "aaa");
        System.out.println("exist4 = " + exist4);

    }

}