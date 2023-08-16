package com.shiyi.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.enums.OrderStatusEnum;
import com.shiyi.common.to.StockDetailTo;
import com.shiyi.common.to.StockLockedTo;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;
import com.shiyi.common.utils.R;
import com.shiyi.common.vo.OrderVo;
import com.shiyi.gulimall.ware.constants.RabbitConstant;
import com.shiyi.gulimall.ware.constants.StorageLockStatusConstant;
import com.shiyi.gulimall.ware.dao.WareSkuDao;
import com.shiyi.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.shiyi.gulimall.ware.entity.WareOrderTaskEntity;
import com.shiyi.gulimall.ware.entity.WareSkuEntity;
import com.shiyi.gulimall.ware.exception.NoStockException;
import com.shiyi.gulimall.ware.feign.OrderFeignService;
import com.shiyi.gulimall.ware.feign.ProductFeignService;
import com.shiyi.gulimall.ware.listener.StockUnLockThread;
import com.shiyi.gulimall.ware.service.IPublishedMsgService;
import com.shiyi.gulimall.ware.service.WareOrderTaskDetailService;
import com.shiyi.gulimall.ware.service.WareOrderTaskService;
import com.shiyi.gulimall.ware.service.WareSkuService;
import com.shiyi.gulimall.ware.vo.OrderItemVo;
import com.shiyi.gulimall.ware.vo.SkuHasStockVo;
import com.shiyi.gulimall.ware.vo.SkuWareHasStock;
import com.shiyi.gulimall.ware.vo.WareSkuLockVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    private static final Logger log = LoggerFactory.getLogger(WareSkuServiceImpl.class);

    @Autowired
    @Qualifier("threadPoolStockUnLockTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolStockUnLockTaskExecutor;

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private IPublishedMsgService publishedMsgService;

    @Autowired
    private PlatformTransactionManager transactionManager;



    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            wrapper.eq("ware_id", wareId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //判断是否有该仓库该商品的库存记录
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        List<WareSkuEntity> wareSkuEntities = wareSkuDao.selectList(wrapper.eq("sku_id", skuId).eq("ware_id", wareId));
        if (null == wareSkuEntities || wareSkuEntities.size() == 0) {
            //插入
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            try {
                //远程查询商品名称，若发生异常不会导致整个事务回滚
                R info = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                wareSkuDao.insert(wareSkuEntity);
            } catch (Exception e) {

            }
        } else {
            //更新
            wareSkuDao.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public Boolean getSkuHasStock(Long skuId,int num) {
        Long stockNum = this.baseMapper.getSkuStock(skuId);
        return stockNum != null && stockNum.intValue() >= num;
    }

    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {

        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            Long stockCount = this.baseMapper.getSkuStock(skuId);

            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(stockCount == null ? false : stockCount > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        return collect;
    }


    @Transactional(rollbackFor = NoStockException.class,propagation = Propagation.REQUIRED)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) throws IOException {

        // 保存库存工作单详情，利于追溯
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

        // 找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            skuWareHasStock.setSkuId(item.getSkuId());
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareIds =  wareSkuDao.listWareIdHasStockBySkuId(item.getSkuId());
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        /**
         * 1、如果每一个商品都锁定成功，将当前商品锁定了几件的工作单记录发给MQ
         * 2、锁定失败，前面保存的工作单信息就回滚了，发送出去的消息，即使要解锁记录，由于去数据库查不到id，故不用解锁库存
         * 但是之前已经锁定了库存的记录，由于库存工作单以及其详细信息表都回滚了，没有记录，只根据MQ中的id是无法得知库存扣减情况的，故
         * 我们应该在MQ中放完整的库存扣减信息
         */
        StockLockedTo stockLockedTo = new StockLockedTo();
        stockLockedTo.setOrderSn(vo.getOrderSn());
        stockLockedTo.setId(wareOrderTaskEntity.getId());
        List<StockDetailTo> details = new ArrayList<>();
        // 锁定库存
        for (SkuWareHasStock skuWareHasStock : collect) {
            boolean skuStocked = false;
            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareId();
            if(wareIds == null || wareIds.size() == 0){
               // 没有仓库有这个商品的库存
                throw new NoStockException(skuId);
            }else{
                for (Long wareId : wareIds) {
                    // 1代表成功，0代表失败
                    Long count = wareSkuDao.lockSkuStock(skuId,wareId,skuWareHasStock.getNum());
                    if(count == 1){
                        skuStocked = true;
                        // 保存库存工作单详细信息
                        WareOrderTaskDetailEntity detailEntity =
                                new WareOrderTaskDetailEntity(null,skuId,"",skuWareHasStock.getNum(),wareOrderTaskEntity.getId(),wareId, StorageLockStatusConstant.LOCKED);
                        wareOrderTaskDetailService.save(detailEntity);
                        StockDetailTo stockDetailTo = new StockDetailTo();
                        BeanUtils.copyProperties(detailEntity,stockDetailTo);
                        details.add(stockDetailTo);
                        break;
                    }
                }
                if(!skuStocked){
                    // 当前商品所有仓库都没有锁住
                    throw new NoStockException(skuId);
                }
            }
        }
        stockLockedTo.setDetails(details);
        // 往RabbitMQ发送消息
        publishedMsgService.saveAndSendMsg(stockLockedTo, "stock-event-exchange", "stock.locked", 120000);

        // 代表全部商品都锁成功了
        return true;
    }


    /**
     * 订单关闭被动触发的库存解锁逻辑，这里不用再去查订单状态了，省去一次查数据库的成本
     * @param orderEntity
     */
    @Override
    public void unlockStock(OrderVo orderEntity) throws InterruptedException,RuntimeException {

        String orderSn = orderEntity.getOrderSn();
        //根据库存工作单来解锁库存，需要查最新的库存工作单的状态，若为已解锁则无需重复解锁了
        WareOrderTaskEntity taskEntity =  wareOrderTaskService.getOrderTaskByOrderSn(orderSn);
        Long taskId = taskEntity.getId();
        //按照工作单id，找到所有没有解锁过的详情单
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(
                new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskId).eq("lock_status", 1));

        unlockStockFunction(orderSn,list);
    }

    /**
     * 库存补偿解锁逻辑:
     * 查询数据库关于这个订单的锁定库存信息
     *           1.1 有：证明库存锁定成功，但需要判断订单状态才能解锁：
     *               1.1.1 订单状态为取消,则需解锁
     *               1.1.2 其他订单状态，不能解锁
     *           1.2 没有：证明库存锁定失败，库存本地事务回滚了，无需解锁
     * @param to
     * @throws InterruptedException
     * @throws RuntimeException
     */
    @Override
    public void unlockStock(StockLockedTo to) throws InterruptedException,RuntimeException {
        List<StockDetailTo> details = to.getDetails();
        String orderSn = to.getOrderSn();
        R response = orderFeignService.getOrderStatus(orderSn);
        if(response.getCode() != 0){
            throw new RuntimeException("rpc调用获取订单状态失败");
        }
        // 获取订单数据
        OrderVo data = response.getData(new TypeReference<OrderVo>() {});
        // 无需解锁库存
        if(data != null && !OrderStatusEnum.CANCLED.getCode().equals(data.getStatus())){
            return;
        }
        List<WareOrderTaskDetailEntity> list = details.stream().map(item -> {
            WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
            BeanUtils.copyProperties(item, detailEntity);
            return detailEntity;
        }).collect(Collectors.toList());

        unlockStockFunction(orderSn,list);
    }


    /**
     * 通用库存解锁函数，多线程消费消息
     * @param details
     * @throws InterruptedException
     * @throws RuntimeException
     */
    public void unlockStockFunction(String orderSn, List<WareOrderTaskDetailEntity> details){
        long startTime = System.currentTimeMillis();
        // 子线程
        CountDownLatch rollBackLatch = new CountDownLatch(1);
        // 主线程
        CountDownLatch mainThreadLatch = new CountDownLatch(details.size());

        AtomicBoolean isThrow = new AtomicBoolean(false);
        // 采用线程池多线程进行解锁库存任务,需要保证并发操作
        for (WareOrderTaskDetailEntity detail : details) {
            threadPoolStockUnLockTaskExecutor.execute(new StockUnLockThread(detail,this,mainThreadLatch,rollBackLatch,isThrow));
        }
        try{
            mainThreadLatch.await(RabbitConstant.STOCK_UNLOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e) {
            isThrow.set(true);
            log.error("******* 订单号[{}]解锁库存超时，库存详情工作单:{}，整体回滚",orderSn,details);
        }
        rollBackLatch.countDown();
        if(isThrow.get()){
            throw new RuntimeException();
        }
        long endTime = System.currentTimeMillis();
        log.info("库存解锁耗时：{}",endTime - startTime);
    }

    // 同步消费
//    @Transactional(rollbackFor = Exception.class)
//    public void unlockStockFunction(String orderSn, List<WareOrderTaskDetailEntity> details){
//        long startTime = System.currentTimeMillis();
//        for (WareOrderTaskDetailEntity detail : details) {
//            if (detail != null && detail.getLockStatus() == StorageLockStatusConstant.LOCKED) {
//                try{
//                    int isUnLock = unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
//                    if (isUnLock != 1) {
//                        log.error("数据库库存解锁失败，库存详情工作单id[{}]将会导致整体回滚", detail.getId());
//                        throw new RuntimeException("数据库库存解锁失败，库存详情工作单id[" + detail.getId() + "]，将会导致整体回滚");
//                    }
//                }catch (Exception e){
//                    log.error("数据库库存解锁失败，库存详情工作单id[{}]将会导致整体回滚", detail.getId());
//                    throw e;
//                }
//            }
//        }
//        long endTime = System.currentTimeMillis();
//        log.info("库存解锁耗时：{}", endTime - startTime);
//    }


    public int unLockStock(Long skuId, Long wareId, Integer skuNum,Long detailId) {

        int flag = wareSkuDao.unLockStock(skuId, wareId, skuNum);
        if(flag  == 1) {
            //更改库存详细单的状态
            LambdaUpdateWrapper<WareOrderTaskDetailEntity> wrapper = Wrappers.<WareOrderTaskDetailEntity>lambdaUpdate()
                    .eq(WareOrderTaskDetailEntity::getId, detailId)
                    .set(WareOrderTaskDetailEntity::getLockStatus, StorageLockStatusConstant.UNLOCKED);
            wareOrderTaskDetailService.update(wrapper);
            log.info("wareId[{}]，skuId[{}]，detailId[{}]解锁成功",wareId,skuId,detailId);
        }
        return flag;
    }

}