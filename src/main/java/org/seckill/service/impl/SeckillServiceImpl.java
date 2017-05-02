package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseExecption;
import org.seckill.exception.SeckillExecption;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by lishi on 2017/4/28.
 *
 * @Component @Service @Dao @Controller
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //md5 盐值字符串，用于混淆 MD5
    private final String slat = "shahofosfowhf37y92y9&9888*(%4%shof";

    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    public Exposer exportSeckillUrl(long seckillId) {
        // 优化点：缓存优化，超时的基础上维护一致性
        // 1：访问 redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            //2:访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                //3：放入 redis
                redisDao.putSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTiem = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTiem.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTiem.getTime());
        }

        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId); //TODO
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillExecption
     * @throws RepeatKillException
     * @throws SeckillCloseExecption 使用注解控制事务方法的有点
     *                               1：开发团队达成一致约定，明确标注事务方法的编程风格。
     *                               2：保证事务方法的执行时间尽可能短，不要穿插其它网络操作，rpc、http 请求 或者剥离到事务方法外部。
     *                               3：不是所有的方法都需要事务，如：只有一条修改，或者读取操作。
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillExecption, RepeatKillException, SeckillCloseExecption {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new SeckillExecption("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存 + 记录购买行为
        try {
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一组合主键
            if (insertCount <= 0) {
                throw new RepeatKillException("seckill repeated");
            } else {
                // 减库存，热点商品竞争
                Date nowTime = new Date();
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新记录，秒杀结束，rollback
                    throw new SeckillCloseExecption("seckill is closed");
                } else {
                    //秒杀成功，commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseExecption e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译期异常 转化为运行期异常
            throw new SeckillExecption("seckill inner error:" + e.getMessage());
        }
    }
}