package com.shiyi.gulimall.ware.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RabbitApplicationEvent<T> extends ApplicationEvent {

    private T data;
    private String messageId;
    private String exchangeName;
    private String routingKey;


    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public RabbitApplicationEvent(Object source, String messageId, String exchangeName, String routingKey) {
        super(source);
        this.data = (T) source;
        this.messageId = messageId;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

}
