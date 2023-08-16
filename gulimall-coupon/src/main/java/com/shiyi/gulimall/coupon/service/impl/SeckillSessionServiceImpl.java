package com.shiyi.gulimall.coupon.service.impl;

import com.shiyi.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.shiyi.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.coupon.dao.SeckillSessionDao;
import com.shiyi.gulimall.coupon.entity.SeckillSessionEntity;
import com.shiyi.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService skuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatest3DaySession() {

        List<SeckillSessionEntity> sessionEntityList = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", getStartTime(), getEndTime()));

        if(sessionEntityList != null && sessionEntityList.size() > 0){
            List<SeckillSessionEntity> sessionWithSkusList = sessionEntityList.stream().map(item -> {

                List<SeckillSkuRelationEntity> relationSkus = skuRelationService.list(
                        new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", item.getId()));
                item.setRelationSkus(relationSkus);
                return item;
            }).collect(Collectors.toList());

            return sessionWithSkusList;
        }

        return null;
    }

    private String getStartTime() {

        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        return LocalDateTime.of(now, min).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getEndTime(){

        LocalDate now = LocalDate.now();
        LocalDate nowPass2Day = now.plusDays(2);
        LocalTime max = LocalTime.MAX;
        return LocalDateTime.of(nowPass2Day, max).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


}