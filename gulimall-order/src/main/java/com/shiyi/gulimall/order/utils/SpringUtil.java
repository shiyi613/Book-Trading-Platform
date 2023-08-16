package com.shiyi.gulimall.order.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpringUtil implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(SpringUtil.class);
    private static ApplicationContext applicationContext;

    public SpringUtil(){

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContextParam) throws BeansException {
        applicationContext = applicationContextParam;
    }

    public static <T> T getBean(Class<T> clazz){
        return clazz == null ? null : applicationContext.getBean(clazz);
    }


    public static <T> T getBean(String beanName){
        return beanName == null ? null : (T) applicationContext.getBean(beanName);
    }

    public static <T> List<T> getBeans(Class<T> classes){
        String[] beanNamesForType = applicationContext.getBeanNamesForType(classes);
        ArrayList<T> list = new ArrayList<>();
        for (String beanName : beanNamesForType) {
            list.add(getBean(beanName));
        }
        return list;
    }

    public static <T> T getBean(String beanName, Class<T> clazz){
        if(null != beanName && !"".equals(beanName.trim())){
            return clazz == null ? null : applicationContext.getBean(beanName,clazz);
        }else{
            return null;
        }
    }

    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static void publishEvent(ApplicationEvent event){
        if(applicationContext != null){
            try {
                applicationContext.publishEvent(event);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }


}
