package com.shiyi.gulimall.order.pay;

import com.shiyi.gulimall.order.utils.SpringUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PayStrategyFactory{

    private PayStrategyFactory(){

    }

//    private List<PayStrategy> strategyList = new ArrayList<>();

    private final Map<Integer,PayStrategy> strategyMap = new HashMap<>();

    private void init(){
        List<PayStrategy> strategyList = SpringUtil.getBeans(PayStrategy.class);
        for (PayStrategy payStrategy : strategyList) {
            strategyMap.put(payStrategy.getType(),payStrategy);
        }
    }

    public static PayStrategyFactory getInstance(){

        PayStrategyFactory paystrategyFactory = Builder.paystrategyFactory;
        paystrategyFactory.init();
        return paystrategyFactory;
    }

    public PayStrategy getStrategy(Integer payType){
        if(!strategyMap.containsKey(payType)){
            return null;
        }
        return strategyMap.get(payType);
    }

    // 通过静态内部类实现单例模式
    private static class Builder{
        private static PayStrategyFactory paystrategyFactory = new PayStrategyFactory();
    }




}
