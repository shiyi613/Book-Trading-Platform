package com.shiyi.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 发布消息表
 * </p>
 *
 * @author shiyi
 * @since 2023-07-17
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Getter
@Setter
@TableName("published_msg")
public class PublishedMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一id
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 内容结构体的类型
     */
    @TableField("class_type")
    private String classType;

    /**
     * 交换机
     */
    @TableField("exchange")
    private String exchange;

    /**
     * 路由键
     */
    @TableField("routing_key")
    private String routingKey;

    /**
     * 状态: 0-新建，1-已发送，2-错误抵达，3-已抵达
     */
    @TableField("status")
    private Integer status;

    /**
     * 重试次数
     */
    @TableField("retries")
    private Integer retries;

    /**
     * 适用于延迟消息，消息延迟的时间，单位ms
     */
    @TableField(value = "delay_time")
    private Integer delayTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 最后一次的更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    public PublishedMsg(String messageId, String content, String classType, String exchange, String routingKey, Integer status, Integer retries) {
        this.messageId = messageId;
        this.content = content;
        this.classType = classType;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.status = status;
        this.retries = retries;
    }

    public PublishedMsg(String messageId, String content, String classType, String exchange, String routingKey, Integer status, Integer retries, Integer delayTime) {
        this.messageId = messageId;
        this.content = content;
        this.classType = classType;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.status = status;
        this.retries = retries;
        this.delayTime = delayTime;
    }
}
