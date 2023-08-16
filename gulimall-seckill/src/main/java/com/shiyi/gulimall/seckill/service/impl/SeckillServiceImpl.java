package com.shiyi.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.shiyi.common.to.SecKillOrderTo;
import com.shiyi.common.to.StockLockedTo;
import com.shiyi.common.utils.R;
import com.shiyi.common.vo.MemberRespVo;
import com.shiyi.gulimall.seckill.feign.CouponFeignService;
import com.shiyi.gulimall.seckill.feign.ProductFeignService;
import com.shiyi.gulimall.seckill.interceptor.LoginInteceptor;
import com.shiyi.gulimall.seckill.service.SeckillService;
import com.shiyi.gulimall.seckill.to.SeckillSkuRedisTo;
import com.shiyi.gulimall.seckill.vo.SeckillSessionsWithSkusVo;
import com.shiyi.gulimall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author:shiyi
 * @create: 2023-03-12  21:47
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    private final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private final String SKUSECKILL_CACHE_PREFIX = "seckill:skus:";
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void uploadSeckillSkuLatest3Days() {
        //1、扫描最近三天需要参与秒杀的活动
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {

            List<SeckillSessionsWithSkusVo> sessionsWithSkusData = r.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {
            });
            if (sessionsWithSkusData != null && sessionsWithSkusData.size() > 0) {
                //上架(缓存到redis中)
                //1、缓存活动信息
                redisSessionsInfo(sessionsWithSkusData);
                //2、缓存活动关联的商品信息
                redisSessionSkusInfo(sessionsWithSkusData);
            }
        }
    }

    /**
     * 返回当前时间可以参与秒杀的商品信息
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {

        //1、确定当前时间属于哪个秒杀场次
        long currentTime = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        //seckill:sessions:11111111_1111222222222
        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] s = replace.split("_");
            long startTime = Long.parseLong(s[0]);
            long endTime = Long.parseLong(s[1]);
            if (currentTime >= startTime && currentTime <= endTime) {
                //2、获取这个秒杀场次的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUSECKILL_CACHE_PREFIX);

                if (range != null && range.size() > 0) {
                    List<String> values = ops.multiGet(range);

                    if (values != null && values.size() > 0) {
                        List<SeckillSkuRedisTo> collect = values.stream().map(item -> {
                            return JSON.parseObject(item, SeckillSkuRedisTo.class);
                        }).collect(Collectors.toList());
                        return collect;
                    }
                }
                break;
            }
        }

        return null;
    }

    /**
     * 从redis获取
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {

        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUSECKILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            String regx = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(regx, key)) {
                    String json = ops.get(key);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);

                    //随机码，只有秒杀才能暴露
                    long currentTime = new Date().getTime();
                    if (currentTime >= seckillSkuRedisTo.getStartTime() &&
                            currentTime <= seckillSkuRedisTo.getEndTime()) {

                    } else {
                        seckillSkuRedisTo.setRandomCode(null);
                    }
                    return seckillSkuRedisTo;
                }
            }
        }
        return null;
    }

    /**
     * 执行秒杀业务
     *
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String seckill(String killId, String key, Integer num) {

        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();

        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUSECKILL_CACHE_PREFIX);
        String json = ops.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SeckillSkuRedisTo redisSkuInfo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //合法性校验
            long currentTime = new Date().getTime();
            Long startTime = redisSkuInfo.getStartTime();
            Long endTime = redisSkuInfo.getEndTime();
            //1、校验秒杀时间
            if (currentTime >= startTime && currentTime <= endTime) {
                //2、检验随机码和商品id
                String randomCode = redisSkuInfo.getRandomCode();
                String skuId = redisSkuInfo.getPromotionSessionId() + "_" + redisSkuInfo.getSkuId();
                if (randomCode.equals(key) && skuId.equals(killId)) {
                    //3、检验购买数量
                    if (num <= redisSkuInfo.getSeckillLimit().intValue()) {
                        //4、校验该用户是否已经购买过该商品，这里采用redis占坑法,key为userId_sessionId_skuId
                        String purchaseKey = memberRespVo.getId() + "_" + skuId;
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(purchaseKey, num.toString(),
                                endTime - currentTime, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            //占坑成功，表示此用户从未秒杀过此商品
                            //5、检验库存信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            boolean flag = semaphore.tryAcquire(num);
                            if (flag) {
                                //秒杀成功，快速下单，发送给MQ消息，让订单服务慢慢创建订单
                                SecKillOrderTo secKillOrderTo = new SecKillOrderTo();
                                String orderSn = IdWorker.getTimeId();
                                secKillOrderTo.setOrderSn(orderSn);
                                secKillOrderTo.setMemberId(memberRespVo.getId());
                                secKillOrderTo.setNum(num);
                                secKillOrderTo.setPromotionSessionId(redisSkuInfo.getPromotionSessionId());
                                secKillOrderTo.setSeckillPrice(redisSkuInfo.getSeckillPrice());
                                secKillOrderTo.setSkuId(redisSkuInfo.getSkuId());
                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", secKillOrderTo);
                                return orderSn;
                            }
                        }
                    }
                }
            }
            return null;
        }
    }

    private void redisSessionsInfo(List<SeckillSessionsWithSkusVo> sessions) {

        sessions.forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;

            if (!redisTemplate.hasKey(key)) {
                List<String> skuIdsBySessionId = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                //value为场次id_商品id
                redisTemplate.opsForList().leftPushAll(key, skuIdsBySessionId);
            }
        });

    }

    private void redisSessionSkusInfo(List<SeckillSessionsWithSkusVo> sessions) {

        sessions.forEach(session -> {

            BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SKUSECKILL_CACHE_PREFIX);
            session.getRelationSkus().forEach(item -> {

                String randomCode = UUID.randomUUID().toString().replace("-", "");

                if (!ops.hasKey(item.getPromotionSessionId().toString() + "_" + item.getSkuId().toString())) {

                    SeckillSkuRedisTo seckillSkuTo = new SeckillSkuRedisTo();

                    //1、sku的基本数据
                    R r = productFeignService.getSkuinfoBySkuId(item.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfoVo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        seckillSkuTo.setSkuInfo(skuInfoVo);
                    }

                    //2、sku的秒杀信息
                    BeanUtils.copyProperties(item, seckillSkuTo);

                    //3、秒杀时间
                    seckillSkuTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuTo.setEndTime(session.getEndTime().getTime());

                    //4、随机码,避免参数名字暴露，被大量流量请求攻击,加了之后变成 seckill?skuId=1&key=xxxxxx
                    seckillSkuTo.setRandomCode(randomCode);

                    String jsonString = JSON.toJSONString(seckillSkuTo);
                    ops.put(item.getPromotionSessionId() + "_" + item.getSkuId().toString(), jsonString);

                    //若当前这个场次的商品的库存信息已经上架就不需要上架
                    //5、信号量作为库存
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                    semaphore.trySetPermits(item.getSeckillCount().intValue());
                }
            });
        });
    }
}
