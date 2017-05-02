-- 执行秒杀存储过程
DELIMITER $$ -- console ; 转换为 $$
-- 定义存储过程
-- 参数：in 输入参数，out 输出参数
-- row_count();sql 函数，返回上一条修改类型 sql 影响的行数。
-- row_count: <0 sql 错误、未执行修改 sql  =0 未修改数据  >0 表示修改的行数
ALTER PROCEDURE `seckill`.`execute_seckill`
(in v_seckill_id bigint,
in v_phone bigint,
in v_kill_time TIMESTAMP,
out r_result int )
  BEGIN
    DECLARE insert_count int DEFAULT 0;
    START TRANSACTION ;
    insert ignore into success_killed(seckill_id,user_phone,create_time)
    values (v_seckill_id,v_phone,v_kill_time);
    select ROW_COUNT() into insert_count;
    IF (insert_count = 0) THEN
      ROLLBACK ;
      SET r_result = -1;
    ELSEIF(insert_count < 0) THEN
      ROLLBACK ;
      SET r_result = -2;
    ELSE
      UPDATE seckill
      SET number = number -1
      WHERE seckill_id = v_seckill_id
      and end_time > v_kill_time
      and start_time < v_kill_time
      and number > 0;
      select ROW_COUNT() into insert_count;
      IF (insert_count = 0) THEN
      ROLLBACK ;
      set r_result = 0;
      ELSEIF (insert_count < 0) THEN
      ROLLBACK ;
      set r_result = -2;
      ELSE
        COMMIT ;
        SET r_result = 1;
      END IF;
    END IF;
  END;
$$
-- 存储过程定义完成

DELIMITER ;

set @r_result = -3;
-- 执行存储过程
call execute_seckill(1000,13502178891,now(),@r_result);
-- 获取结果
select @r_result;

-- 存储过程
-- 1.存储过程优化：事务行级锁持有的时间
-- 2.不要过度依赖存储过程
-- 3.简单的逻辑可以使用存储过程
-- 4.一个秒杀单6000/qps