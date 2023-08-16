package com.shiyi.gulimall.ware.listener;

import com.shiyi.gulimall.ware.constants.StorageLockStatusConstant;
import com.shiyi.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.shiyi.gulimall.ware.service.impl.WareSkuServiceImpl;
import com.shiyi.gulimall.ware.utils.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class StockUnLockThread implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(StockUnLockThread.class);
    private PlatformTransactionManager transactionManager = SpringUtil.getBean(PlatformTransactionManager.class);
    private WareOrderTaskDetailEntity detail;
    private WareSkuServiceImpl wareSkuService;
    private CountDownLatch mainThreadLatch;
    private CountDownLatch rollBackLatch;
    private AtomicBoolean isThrow;
    private Random random = new Random();


    public StockUnLockThread(WareOrderTaskDetailEntity detail,
                             WareSkuServiceImpl wareSkuService,
                             CountDownLatch mainThreadLatch,
                             CountDownLatch rollBackLatch,
                             AtomicBoolean isThrow){
        this.detail = detail;
        this.wareSkuService = wareSkuService;
        this.mainThreadLatch = mainThreadLatch;
        this.rollBackLatch = rollBackLatch;
        this.isThrow = isThrow;
    }

    @Override
    public void run() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);
        try{
            // 只有库存工作详情单有，并且其状态为已锁定状态才能解锁
            if(detail != null && detail.getLockStatus() == StorageLockStatusConstant.LOCKED){
                int isUnLock = wareSkuService.unLockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
                if(isUnLock != 1){
                    isThrow.set(true);
                    log.error("数据库库存解锁失败，库存详情工作单id[{}]将会导致整体回滚",detail.getId());
                }
            }
        }catch (Exception e){
            isThrow.set(true);
            log.error("数据库库存解锁失败，库存详情工作单id[{}]将会导致整体回滚",detail.getId());
        }

        try {
            mainThreadLatch.countDown();
            rollBackLatch.await();
            if(isThrow.get()){
                transactionManager.rollback(status);
            }else{
                transactionManager.commit(status);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
