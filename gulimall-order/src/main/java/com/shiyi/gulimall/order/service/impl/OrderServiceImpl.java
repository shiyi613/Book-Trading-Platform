package com.shiyi.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiyi.common.to.SecKillOrderTo;
import com.shiyi.common.utils.PageUtils;
import com.shiyi.common.utils.Query;
import com.shiyi.common.utils.R;
import com.shiyi.common.vo.*;
import com.shiyi.gulimall.order.constant.OrderConstant;
import com.shiyi.gulimall.order.dao.OrderDao;
import com.shiyi.gulimall.order.entity.OrderEntity;
import com.shiyi.gulimall.order.entity.OrderItemEntity;
import com.shiyi.gulimall.order.entity.PaymentInfoEntity;
import com.shiyi.gulimall.order.enume.OrderStatusEnum;
import com.shiyi.gulimall.order.feign.*;
import com.shiyi.gulimall.order.interceptor.LoginInteceptor;
import com.shiyi.gulimall.order.service.IPublishedMsgService;
import com.shiyi.gulimall.order.service.OrderItemService;
import com.shiyi.gulimall.order.service.OrderService;
import com.shiyi.gulimall.order.service.PaymentInfoService;
import com.shiyi.gulimall.order.to.OrderCreateTo;
import com.shiyi.gulimall.order.vo.*;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    public ThreadLocal<OrderSubmitVo> orderSubmitVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String CART_PREFIX = "gulimall:cart:";

    @Autowired
    public ThreadPoolExecutor executor;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Autowired
    private IPublishedMsgService publishedMsgService;

    @Autowired
    private CouponFeignService couponFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String) params.get("key");
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq(key != null && !key.equals(""), "id", key)
                        .or()
                        .eq(key != null && !key.equals(""), "member_id", key)

        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        // 获取用户登录信息
        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 异步获取收货地址
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //解决异步任务ThreadLocal数据不共享问题，即获取不到老请求问题
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询所有收货地址
            List<MemberAddressVo> address = memberFeignService.getAddress(memberRespVo.getId());
            confirmVo.setAddress(address);
        }, executor);
        // 异步获取购物车信息
        CompletableFuture<Void> cartItemsFuture = CompletableFuture.runAsync(() -> {
            //解决异步任务ThreadLocal数据不共享问题，即获取不到老请求问题
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询购物车所选中的购物项
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(currentUserCartItems);
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getItems();
            List<Long> collect = items.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R skuHasStock = wareFeignService.getSkuHasStock(collect);
            if (skuHasStock.getCode() == 0) {
                List<SkuStockVo> data = skuHasStock.getData(new TypeReference<List<SkuStockVo>>() {
                });
                if (data != null && data.size() > 0) {
                    Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                    confirmVo.setStocks(map);
                }
            }
        }, executor);

        // 查询可用优惠券
        CompletableFuture<Void> couponsFuture = CompletableFuture.runAsync(() -> {
            //解决异步任务ThreadLocal数据不共享问题，即获取不到老请求问题
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 该用户领取的所有优惠券
            List<MemberCouponVo> currentUserCoupons = memberFeignService.getCurrentUserCoupons();
            // 拿到优惠券详细信息
            List<Long> ids = currentUserCoupons.stream().map(item -> item.getCid()).collect(Collectors.toList());
            List<CouponTradeVo> collect = new ArrayList<>();
            if (ids.size() != 0) {
                List<CouponVo> coupons = couponFeignService.getInfosByIds(ids);
                // 拿到购物车需要结算的商品id，及对应分类id
                List<OrderItemVo> items = confirmVo.getItems();
                List<Long> spuIds = items.stream().map(item -> item.getSpuId()).collect(Collectors.toList());
                List<Long> categoryIds = productFeignService.getCategoryIdsBySpuIds(spuIds);

                // 筛选出可用优惠券
                collect = coupons.stream().filter(item -> {
                    if (item.getMinPoint().compareTo(confirmVo.getPayPrice()) > 0) {
                        return false;
                    }
                    return true;
                }).filter(item -> {
                    if (item.getUseType() == 0) {
                        return true;
                    } else if (item.getUseType() == 1) {
                        List<Long> cateIds = couponFeignService.getCategoryId(item.getId());
                        for (Long categoryId : categoryIds) {
                            if (cateIds.contains(categoryId)) {
                                return true;
                            }
                        }
                    } else {
                        List<Long> spuIdList = couponFeignService.getSpuId(item.getId());
                        for (Long spuId : spuIds) {
                            if (spuIdList.contains(spuId)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }).map(item -> {
                    CouponTradeVo couponTradeVo = new CouponTradeVo();
                    couponTradeVo.setId(item.getId());
                    couponTradeVo.setCouponName(item.getCouponName());
                    couponTradeVo.setAmount(item.getAmount());
                    return couponTradeVo;
                }).collect(Collectors.toList());
            }
            confirmVo.setCouponTradeVo(collect);
        });


        //查询用户积分（京豆）
        Integer integration = memberRespVo.getIntegration();
        confirmVo.setIntegration(integration);

        //其他数据自动计算

        //防重令牌（幂等性）
        String token = UUID.randomUUID().toString().replace("-", "");
        //给服务器
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId(), token, 30, TimeUnit.MINUTES);

        //给网页
        confirmVo.setOrderToken(token);


        try {
            CompletableFuture.allOf(addressFuture, cartItemsFuture, couponsFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return confirmVo;
    }


    // TODO：优化提交订单逻辑
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) throws IOException {

        long startTime = System.currentTimeMillis();
        orderSubmitVoThreadLocal.set(vo);

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();

        // 验证令牌(令牌获取对比和删除令牌必须是原子操作)
        // 0代表令牌对比失败或者令牌删除失败，1代表令牌删除成功
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberRespVo.getId()), orderToken);

        if (0L == result) {
            // 失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            RLock lock = redissonClient.getLock("cart-" + memberRespVo.getId() + "-lock");
            try {
                // 给购物车加锁
                lock.tryLock(1, TimeUnit.SECONDS);
                // 创建订单、锁库存......
                OrderCreateTo order = createOrder();
                vo.setCouponFare(vo.getCouponFare() == null ? new BigDecimal(0) : vo.getCouponFare());
                order.getOrder().setPayAmount(order.getOrder().getPayAmount().subtract(vo.getCouponFare()));

                // 验证价格（网页提交的数据和后台计算的）
                BigDecimal payAmount = order.getOrder().getPayAmount();
                BigDecimal payPrice = vo.getPayPrice();
                if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                    // TODO:这里采用直接组织数据写表的方式
                    // 保存订单到数据库
                    saveOrder(order);
                    // 锁定库存
                    WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                    wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                    List<OrderItemVo> lockStockItems = order.getOrderItems().stream().map(item -> {
                        OrderItemVo orderItemVo = new OrderItemVo();
                        orderItemVo.setSkuId(item.getSkuId());
                        orderItemVo.setTitle(item.getSkuName());
                        orderItemVo.setCount(item.getSkuQuantity());
                        return orderItemVo;
                    }).collect(Collectors.toList());
                    wareSkuLockVo.setLocks(lockStockItems);

                    // 同步远程锁库存，这里需要分布式事务，当库存服务执行成功，但返回结果超时或者丢失 / 此句后面的代码逻辑出现错误
                    // 就会导致订单可以回滚，但是库存服务不能回滚
                    R r = wareFeignService.orderLockStock(wareSkuLockVo);
                    if (r.getCode() == 0) {
                        // 锁定成功
                        responseVo.setOrder(order.getOrder());
                        responseVo.setCode(0);
                        // 同步消息入库，异步给MQ发送消息
                        publishedMsgService.saveAndSendMsg(order.getOrder(), "order-event-exchange", "order.create.order", 1800000);
                        // 清空购物车所选项
                        BoundHashOperations<String, Object, String> hashOperations = redisTemplate.boundHashOps(CART_PREFIX + memberRespVo.getId());
                        List<String> skuIdList = order.getOrderItems().stream().map(item -> String.valueOf(item.getSkuId())).collect(Collectors.toList());
                        for (String skuId : skuIdList) {
                            hashOperations.delete(skuId);
                        }
                        // 扣减优惠券
                        if (vo.getCouponId() != null) {
                            memberFeignService.deleteCouponNum(vo.getCouponId());
                        }

                        List<SkuInfoVo> skuInfoVoList = order.getOrderItems().stream().map(item -> {
                            SkuInfoVo skuInfoVo = new SkuInfoVo();
                            skuInfoVo.setSkuId(item.getSkuId());
                            skuInfoVo.setSaleCount(Long.valueOf(item.getSkuQuantity()));
                            return skuInfoVo;
                        }).collect(Collectors.toList());
                        // sku销量增加
                        productFeignService.saleCountBatchAdd(skuInfoVoList);
                        long endTime = System.currentTimeMillis();
                        log.info(">>> 订单号[{}]提交流程耗时[{}]", order.getOrder().getOrderSn(), endTime - startTime);
                        return responseVo;
                    } else {
                        //锁定失败
                        throw new RuntimeException("3");
                    }
                } else {
                    throw new RuntimeException("2");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("uid:" + memberRespVo.getId() + "获取购物车锁失败");
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {

        return this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭订单
     *
     * @param entity
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder(OrderEntity entity) throws IOException {
        // 查询当前订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            //将待付款的订单关闭，注意这里不要用拿到消息里面的实体来更新状态，由于30分钟后订单的很多属性可能发生变化
            OrderEntity update = new OrderEntity();
            update.setId(orderEntity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            LambdaUpdateWrapper<OrderEntity> wrapper = Wrappers.<OrderEntity>lambdaUpdate()
                    .eq(OrderEntity::getId, orderEntity.getId())
                    .set(OrderEntity::getStatus, OrderStatusEnum.CANCLED.getCode());
            this.update(wrapper);
            // 给库存服务发送库存解锁消息
            OrderVo orderVo = new OrderVo();
            BeanUtils.copyProperties(entity, orderVo);
            publishedMsgService.saveAndSendMsg(orderVo, "order-event-exchange", "order.release.other.#", 0);
            // TODO：退还优惠券
        }
    }


    /**
     * 根据订单号封装好要支付的信息
     *
     * @param orderSn
     * @return
     */
    @Override
    public PayVo getOrderPay(String orderSn) {

        PayVo payVo = new PayVo();

        OrderEntity orderEntity = this.getOrderStatus(orderSn);

        //只能两位小数
        payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2, RoundingMode.UP).toEngineeringString());
        payVo.setOut_trade_no(orderSn);
        List<OrderItemEntity> orderItemEntityList = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        OrderItemEntity orderItemEntity = orderItemEntityList.get(0);
        payVo.setSubject(orderItemEntity.getSkuName());
        payVo.setBody(orderItemEntity.getSkuAttrsVals());

        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {

        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberRespVo.getId()).orderByDesc("id")
        );

        List<OrderEntity> orderWithItems = page.getRecords().stream().map(item -> {
            List<OrderItemEntity> items = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", item.getOrderSn()));
            item.setItemEntities(items);
            return item;
        }).collect(Collectors.toList());

        page.setRecords(orderWithItems);

        return new PageUtils(page);
    }

    /**
     * 处理支付宝的支付结果
     *
     * @param vo
     * @return
     */
    @Override
    public String handlePayResult(PayAsyncVo vo) {

        //1、保存交易流水信息
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setAlipayTradeNo(vo.getTrade_no());
        paymentInfoEntity.setOrderSn(vo.getOut_trade_no());
        paymentInfoEntity.setSubject(vo.getSubject());
        paymentInfoEntity.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        paymentInfoEntity.setPaymentStatus(vo.getTrade_status());
        paymentInfoEntity.setCallbackTime(vo.getNotify_time());

        paymentInfoService.save(paymentInfoEntity);

        //2、修改订单的状态
        if (vo.getTrade_status().equals("TRADE_SUCCESS") || vo.getTrade_status().equals("TRADE_FINISHED")) {
            String outTradeNo = vo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(outTradeNo, OrderStatusEnum.PAYED.getCode());
        }

        return "success";

    }

    @Override
    public void createSeckillOrder(SecKillOrderTo to) {

        //保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(to.getOrderSn());
        orderEntity.setMemberId(to.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = to.getSeckillPrice().multiply(new BigDecimal(String.valueOf(to.getNum())));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);

        //保存订单项信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(to.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(to.getNum());
        orderItemService.save(orderItemEntity);
    }


    /**
     * 保存订单数据
     *
     * @param order
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderDao.insert(orderEntity);

        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }


    /**
     * 创建总订单框架
     *
     * @return
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //1、根据订单号创建订单
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = bulidOrder(orderSn);

        //2、购物商品项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        //3、价格计算
        computePrice(order, orderItemEntities);

        orderCreateTo.setOrder(order);
        orderCreateTo.setOrderItems(orderItemEntities);

        return orderCreateTo;
    }

    /**
     * 设置总订单的价格信息
     *
     * @param order
     * @param orderItemEntities
     */
    private void computePrice(OrderEntity order, List<OrderItemEntity> orderItemEntities) {

        BigDecimal total = new BigDecimal("0");
        BigDecimal integrationTotal = new BigDecimal("0");
        BigDecimal couponTotal = new BigDecimal("0");
        BigDecimal promotionTotal = new BigDecimal("0");
        BigDecimal giftGrowthTotal = new BigDecimal("0");
        BigDecimal giftIntegrationTotal = new BigDecimal("0");

        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            BigDecimal realAmount = orderItemEntity.getRealAmount();
            integrationTotal = integrationTotal.add(orderItemEntity.getIntegrationAmount()); //积分
            couponTotal = couponTotal.add(orderItemEntity.getCouponAmount());   //优惠券
            promotionTotal = promotionTotal.add(orderItemEntity.getPromotionAmount());    //促销
            total = total.add(realAmount);     //订单总额
            giftGrowthTotal = giftGrowthTotal.add(new BigDecimal(orderItemEntity.getGiftGrowth().toString()));    //可以获得的成长值
            giftIntegrationTotal = giftIntegrationTotal.add(new BigDecimal(orderItemEntity.getGiftIntegration().toString()));  //可以获得的积分值
        }

        //优惠信息
        order.setIntegrationAmount(integrationTotal);
        order.setCouponAmount(couponTotal);
        order.setPromotionAmount(promotionTotal);

        //可以获得的积分、成长值
        order.setIntegration(giftIntegrationTotal.intValue());
        order.setGrowth(giftGrowthTotal.intValue());

        //订单总额
        order.setTotalAmount(total);

        //应付总额 = 订单总额 + 物流费用
        order.setPayAmount(total.add(order.getFreightAmount()));

        order.setDeleteStatus(0);
    }

    /**
     * 设置订单信息
     *
     * @param orderSn
     * @return
     */
    private OrderEntity bulidOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();

        MemberRespVo memberRespVo = LoginInteceptor.loginUser.get();
        //用户信息
        orderEntity.setMemberId(memberRespVo.getId());
        orderEntity.setMemberUsername(memberRespVo.getNickname());

        //订单号
        orderEntity.setOrderSn(orderSn);
        //2、收货地址信息
        R fare = wareFeignService.getFare(orderSubmitVoThreadLocal.get().getAddrId());
        if (fare.getCode() == 0) {
            FareVo data = fare.getData(new TypeReference<FareVo>() {
            });
            //物流费用
            orderEntity.setFreightAmount(data.getFare());
            //收货人信息
            orderEntity.setReceiverCity(data.getAddress().getCity());
            orderEntity.setReceiverDetailAddress(data.getAddress().getDetailAddress());
            orderEntity.setReceiverName(data.getAddress().getName());
            orderEntity.setReceiverPhone(data.getAddress().getPhone());
            orderEntity.setReceiverPostCode(data.getAddress().getPostCode());
            orderEntity.setReceiverProvince(data.getAddress().getProvince());
            orderEntity.setReceiverRegion(data.getAddress().getRegion());
            //订单状态
            orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
            orderEntity.setAutoConfirmDay(7);
            return orderEntity;
        }
        return null;
    }

    /**
     * 设置订单商品项全部信息
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {

        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> collect = currentUserCartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = buildOrderItem(item);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }


    /**
     * 设置某个订单商品项的信息
     *
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        //包含订单号、商品SPU、SKU、积分信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        //商品的SPU信息
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(item.getSkuId());
        if (spuInfoBySkuId.getCode() == 0) {
            SpuInfoVo data = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
            });
            orderItemEntity.setSpuId(data.getId());
            orderItemEntity.setSpuName(data.getSpuName());
            orderItemEntity.setSpuBrand(data.getBrandId().toString());
            orderItemEntity.setCategoryId(data.getCatalogId());
        }

        //商品的SKU信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"));

        //积分信息
        orderItemEntity.setGiftGrowth((item.getPrice().multiply(new BigDecimal(item.getCount().toString()))).intValue());
        orderItemEntity.setGiftIntegration((item.getPrice().multiply(new BigDecimal(item.getCount().toString()))).intValue());

        //订单项价格
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        //当前订单项的实际金额
        orderItemEntity.setRealAmount(orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString())));

        return orderItemEntity;
    }
}