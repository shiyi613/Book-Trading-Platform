package com.shiyi.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.shiyi.common.utils.R;
import com.shiyi.gulimall.ware.feign.MemberFeignService;
import com.shiyi.gulimall.ware.vo.FareVo;
import com.shiyi.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.ware.dao.WareInfoDao;
import com.shiyi.gulimall.ware.entity.WareInfoEntity;
import com.shiyi.gulimall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String)params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.eq("id",key).or().like("name",key)
                    .or().like("address",key)
                    .or().like("areacode",key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    /**
     * 根据收货地址计算运费
     * @param addrId
     * @return
     */
    @Override
    public FareVo getFare(Long addrId) {

        FareVo fareVo = new FareVo();
        R addressInfo = memberFeignService.getAddressInfo(addrId);
        if(addressInfo.getCode() == 0){
            MemberAddressVo data = addressInfo.getData("memberReceiveAddress",new TypeReference<MemberAddressVo>() {});
            if(data != null){
                //这里应该调用各种物流公司的API，简化操作以手机号最后一位为物流费用
                char fare = data.getPhone().charAt(data.getPhone().length() - 1);
                fareVo.setFare(new BigDecimal(String.valueOf(fare)));
                fareVo.setAddress(data);
                return fareVo;
            }
        }
        return null;
    }

}