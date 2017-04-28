package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by lishi on 2017/4/27.
 * des: command + shift + t 创建测试快捷键
 *
 * 配置 spring 和 junit 整合，junit 启动时加载 springIOC 容器
 * spring-test，junit
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {

    @Resource
    private SeckillDao seckillDao;

    @Test
    public void queryById() throws Exception {
        long id = 1000;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.toString());
    }

    @Test
    public void queryAll() throws Exception {
        // java 没有保存形参的记录：queryAll（int offset，int limit） -> queryAll（arg0，arg1）
        List<Seckill> seckills = seckillDao.queryAll(0,10);
        for (Seckill seckill : seckills) {
            System.out.println(seckill);
        }
    }

    @Test
    public void reduceNumber() throws Exception {
        Date killTime = new Date();
        int count = seckillDao.reduceNumber(1000L,killTime);
        System.out.println("count = " + count);
    }

}