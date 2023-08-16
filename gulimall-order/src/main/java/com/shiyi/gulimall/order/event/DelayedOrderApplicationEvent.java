package com.shiyi.gulimall.order.event;

public class DelayedOrderApplicationEvent extends RabbitApplicationEvent {


    /**
     * Create a new ApplicationEvent.
     *
     * @param source       the object on which the event initially occurred (never {@code null})
     * @param messageId
     * @param exchangeName
     * @param routingKey
     */
    public DelayedOrderApplicationEvent(Object source, String messageId, String exchangeName, String routingKey) {
        super(source, messageId, exchangeName, routingKey);
    }
}
