package com.shiyi.gulimall.ware;

import com.shiyi.gulimall.ware.dao.PublishedMsgMapper;
import com.shiyi.gulimall.ware.entity.PublishedMsg;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallWareApplicationTests {

    @Autowired
    private PublishedMsgMapper publishedMsgMapper;

    @Test
    void contextLoads() {
//        PublishedMsg build = PublishedMsg.builder().messageId("123").content("123").classType("123").exchange("123").retries(1).build();
//        publishedMsgMapper.insert(build);
        List<PublishedMsg> messageByStatus = publishedMsgMapper.findMessageByStatusAndDelayTime(4);
        System.out.println();
    }

}
