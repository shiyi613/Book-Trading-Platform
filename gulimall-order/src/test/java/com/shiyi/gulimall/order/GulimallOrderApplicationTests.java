package com.shiyi.gulimall.order;

import com.shiyi.gulimall.order.dao.OrderDao;
import com.shiyi.gulimall.order.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderDao orderDao;

    @Test
    void contextLoads() {
        orderDao.insert(new OrderEntity());
    }

}
