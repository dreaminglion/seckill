package org.seckill.dao;

import org.seckill.entity.SuccessKilled;

/**
 * Created by lishi on 2017/4/26.
 */
public interface SuccessKilledDao {

    /**
     * 插入购买明细，可过滤重复
     * @param seckillId
     * @param userPhone
     * @return 插入的行数，0（一行都没插入）表示插入失败
     */
    int insertSuccessKilled(long seckillId,long userPhone);

    /**
     * 根据 id 查询 successKilled 并携带秒杀产品对象实体
     * @param seckillId
     * @return
     */
    SuccessKilled queryByIdWithSeckill(long seckillId);
}
