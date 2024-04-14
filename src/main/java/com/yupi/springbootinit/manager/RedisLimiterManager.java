package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    public void doLimiter(String key){
        //创建限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        //没秒不能超过两次请求
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        //获取令牌
        final int num=1;  //一次请求的令牌数
        boolean acquired = rateLimiter.tryAcquire(num);
        if(!acquired){  //被限制了
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }

}
