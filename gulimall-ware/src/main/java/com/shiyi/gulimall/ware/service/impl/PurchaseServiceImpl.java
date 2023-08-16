package com.shiyi.gulimall.ware.service.impl;

import com.shiyi.common.constant.WareConstant;
import com.shiyi.gulimall.ware.entity.PurchaseDetailEntity;
import com.shiyi.gulimall.ware.service.PurchaseDetailService;
import com.shiyi.gulimall.ware.service.WareSkuService;
import com.shiyi.gulimall.ware.vo.MergeVo;
import com.shiyi.gulimall.ware.vo.PurchaseDoneVo;
import com.shiyi.gulimall.ware.vo.PurchaseItemDoneVo;
import com.sun.deploy.ui.DialogTemplate;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;

import com.shiyi.gulimall.ware.dao.PurchaseDao;
import com.shiyi.gulimall.ware.entity.PurchaseEntity;
import com.shiyi.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>() //.eq("status",0).or().eq("status",1)
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if(purchaseId == null){      //新建采购单
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setStatus(WareConstant.purchaseEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        //确认采购单状态是新建或已分配
        PurchaseEntity byId = this.getById(purchaseId);
        Integer status = byId.getStatus();
        //确认采购需求项都是新建或已分配状态
        List<Long> itemIdList = mergeVo.getItems();
        Collection<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(itemIdList);
        for (PurchaseDetailEntity item : purchaseDetailEntities) {
            if(item.getStatus() != WareConstant.purchaseDetailStatusEnum.CREATED.getCode() &&
                    item.getStatus() != WareConstant.purchaseDetailStatusEnum.ASSIGNED.getCode() ){
                return;
            }
        }

        if(status == 0 || status == 1){
            List<Long> items = mergeVo.getItems();
            Long finalPurchaseId  = purchaseId;
            List<PurchaseDetailEntity> collect = items.stream().map(item -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(item);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(WareConstant.purchaseDetailStatusEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect);

            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(purchaseId);
            purchaseEntity.setUpdateTime(new Date());
            this.updateById(purchaseEntity);
        }

    }

    @Override
    public void receivePurchase(List<Long> ids) {
        //1、确认当前采购单是新建或已分配状态
        List<PurchaseEntity> collect = ids.stream().map(id -> {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item -> {
            if (item.getStatus() == WareConstant.purchaseEnum.CREATED.getCode() ||
                    item.getStatus() == WareConstant.purchaseEnum.ASSIGNED.getCode()) {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.purchaseEnum.RECEIVED.getCode());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2、改变采购单状态
        this.updateBatchById(collect);

        //3、改变采购需求项状态
        collect.forEach(item -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(entity -> {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(entity.getId());
                purchaseDetailEntity.setStatus(WareConstant.purchaseDetailStatusEnum.BUYING.getCode());
                return purchaseDetailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Transactional
    @Override
    public void finishPurchase(PurchaseDoneVo doneVo) {

        //1、改变采购单的采购需求项的状态
        List<PurchaseItemDoneVo> items = doneVo.getItems();
        Boolean flag  = true;
        List<PurchaseDetailEntity> list = new ArrayList<>();

        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstant.purchaseDetailStatusEnum.HASERROR.getCode()){
                flag = false;
                purchaseDetailEntity.setStatus(item.getStatus());
            }else{
                purchaseDetailEntity.setStatus(WareConstant.purchaseDetailStatusEnum.FINISHED.getCode());
                //3、将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());
            }
            purchaseDetailEntity.setId(item.getItemId());
            list.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(list);

        //2、改变采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(doneVo.getId());
        purchaseEntity.setStatus(flag?WareConstant.purchaseEnum.FINISHED.getCode():WareConstant.purchaseEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}