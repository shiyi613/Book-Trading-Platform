package com.shiyi.gulimall.order.event;

import com.shiyi.gulimall.order.entity.PublishedMsg;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RetryStoreApplicationEvent<T> extends ApplicationEvent {

    private PublishedMsg data;
    // 以下属性属于RabbitApplicationEvent
    private T rabbitData;
    private String msgId;
    private String exchangeName;
    private String routingKey;
    private RabbitApplicationEvent<T> rabbitApplicationEvent;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public RetryStoreApplicationEvent(Object source, T rabbitData, String msgId, String exchangeName, String routingKey) {
        super(source);
        this.data = (PublishedMsg) source;
        this.rabbitData = rabbitData;
        this.msgId = msgId;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.rabbitApplicationEvent = new RabbitApplicationEvent<T>(rabbitData,msgId,exchangeName,routingKey);
    }

}
