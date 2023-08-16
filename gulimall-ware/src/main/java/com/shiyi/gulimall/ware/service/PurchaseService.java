package com.shiyi.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.gulimall.ware.entity.PurchaseEntity;
import com.shiyi.gulimall.ware.vo.MergeVo;
import com.shiyi.gulimall.ware.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author shiyi
 * @email 511665483@qq.com
 * @date 2023-02-21 16:31:11
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void receivePurchase(List<Long> ids);

    void finishPurchase(PurchaseDoneVo doneVo);
}

