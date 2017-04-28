package org.seckill.exception;

/**
 * 秒杀关闭异常
 * Created by lishi on 2017/4/28.
 */
public class SeckillCloseExecption extends SeckillExecption {

    public SeckillCloseExecption(String message) {
        super(message);
    }

    public SeckillCloseExecption(String message, Throwable cause) {
        super(message, cause);
    }
}
