package org.seckill.exception;

/**
 * 重复秒杀异常（运行期异常）
 * Created by lishi on 2017/4/28.
 */
public class RepeatKillException extends SeckillExecption {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
