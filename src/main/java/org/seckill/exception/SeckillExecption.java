package org.seckill.exception;

/**
 * Created by lishi on 2017/4/28.
 */
public class SeckillExecption extends RuntimeException {

    public SeckillExecption(String message) {
        super(message);
    }

    public SeckillExecption(String message, Throwable cause) {
        super(message, cause);
    }
}
