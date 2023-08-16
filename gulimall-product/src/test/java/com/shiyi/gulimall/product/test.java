package com.shiyi.gulimall.product;

import lombok.ToString;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

/**
 * @Author:shiyi
 * @create: 2023-03-15  22:00
 */
@ToString
@Service
public class test {

        int key;
        int value;

        public test(){

        }


        public test(int key,int value){
            this.key = key;
            this.value = value;
        }

        @Bean
        public test getTest(){
            return new test(1,2);
        }
}

