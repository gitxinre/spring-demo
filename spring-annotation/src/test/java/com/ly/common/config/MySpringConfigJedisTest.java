package com.ly.common.config;

import com.ly.dto.User;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.UUID;

/**
 * @author Administrator
 * @date 2018/12/5 11:37
 */

public class MySpringConfigJedisTest {

    private Jedis jedis;

    @Before
    public void initJedis() {
        JedisPool pool = new JedisPool("192.168.100.109", 6379);
        jedis = pool.getResource();
    }

    @Test
    public void testString() {
        jedis.set("a:b:c:d", "20181210001");
    }
    @Test
    public void testJedis() {

        // pool jedis
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(MySpringConfigJedis.class);
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanName :
                beanDefinitionNames) {
            System.out.println("beanName = " + beanName);
        }
        JedisPool pool = (JedisPool) applicationContext.getBean("jedisPool");
        System.out.println("pool = " + pool);
        Jedis jedis = pool.getResource();
        System.out.println("jedis = " + jedis);


        // string
        User user02 = new User();
        user02.setId(UUID.randomUUID().toString());
        user02.setAge(30);
        user02.setName("fffffff");
        JSONObject jsonObject = JSONObject.fromObject(user02);
        String string = jsonObject.toString();
        System.out.println("string = " + string);
        jedis.set("user02", string);
        String user021 = jedis.get("user02");
        JSONObject jsonObject1 = JSONObject.fromObject(user021);
        User u = (User) JSONObject.toBean(jsonObject1, User.class);
        System.out.println("u = " + u);
        System.out.println("u.getClass() = " + u.getClass());


        // hash
        jedis.hset("key", "field", "value");

        jedis.hset("user01", "id", UUID.randomUUID().toString());
        jedis.hset("user01", "name", "yyyyyyy");
        jedis.hset("user01", "age", "31");
        jedis.hset("user01", "address", "高家庄");


        String hget = jedis.hget("user01", "address");
        System.out.println("hget = " + hget);

        //  list
        jedis.lpush("lpush", "hello");
        jedis.lpush("lpush", "world");
        List<String> lpush = jedis.lrange("lpush", 0, -1);
        for (String str :
                lpush) {
            System.out.println("lpush = " + str);
        }
        jedis.rpush("rpush", "hello");
        jedis.rpush("rpush", "world");
        List<String> rpush = jedis.lrange("rpush", 0, -1);
        for (String str :
                rpush) {
            System.out.println("rpush = " + str);
        }

        Long lrem = jedis.lrem("lpush", 1, "str1");
        System.out.println("lrem = " + lrem);


        pool.close();
        jedis.close();

    }

    @Test
    public void testList() {
        String key = "lpush";
        printListItem(key);
        String lpop = jedis.rpop(key);
        System.out.println("=============> lpop = " + lpop);
        printListItem(key);



    }

    private void printListItem(String key) {
        List<String> lrange = jedis.lrange(key, 0, -1);
        for (String item :
                lrange) {
            System.out.println("item = " + item);
        }
        System.out.println("====================print end!====================");
    }

}