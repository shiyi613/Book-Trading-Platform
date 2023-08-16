package com.shiyi.gulimall.seckill.scheduled;

import com.shiyi.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 秒杀商品的定时上架（闲时每天晚上3点上架最近三天的秒杀商品）
 * @Author:shiyi
 * @create: 2023-03-12  21:38
 */
@Slf4j
@Service
public class SeckillSkuScheduled {

    private final String UPLOAD_LOCK = "seckill:upload:lock";

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 当天00:00:00 - 23:59:59
     * 明天00:00:00 - 23:59:59
     * 后天00:00:00 - 23:59:59
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void uploadSeckillSkuLatest3Days(){
        //重复上架无需处理

        //分布式锁
        RLock lock = redissonClient.getLock(UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }

    }
}
