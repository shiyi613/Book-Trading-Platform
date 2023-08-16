package com.shiyi.gulimall.authserver;

import com.shiyi.gulimall.authserver.feign.ThirdPartyFeignService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

@SpringBootTest
class GulimallAuthServerApplicationTests {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Test
    void contextLoads() {

        String code = UUID.randomUUID().toString().substring(0, 5);
        thirdPartyFeignService.sendCode("15812538806","8806");
        LinkedList<Integer> integers = new LinkedList<>();
        ArrayList<ArrayList<Integer>> arrayLists = new ArrayList<>();
        arrayLists.add(new ArrayList<>(integers));
    }

}
