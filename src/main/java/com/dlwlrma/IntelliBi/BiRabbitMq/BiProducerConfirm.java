package com.dlwlrma.IntelliBi.BiRabbitMq;


import com.dlwlrma.IntelliBi.common.ErrorCode;
import com.dlwlrma.IntelliBi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 生产者发送消息的回调接口
 */

@Component
@Slf4j
public class BiProducerConfirm implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 消息无法发送到交换机时执行
     *
     * @param correlationData 保存相关回调消息的id以及相关信息
     * @param result          消息成功发送的回调 f表示发送失败 t表示发送成功
     * @param cause           成功是为null 失败时结果为失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean result, String cause) {
        //得到消息id
        String messageId = correlationData != null ? correlationData.getId() : "";
        if (result) {
            log.info("消息发送到交换机成功,id为 :" + messageId);
        } else {
            log.info("消息发送失败,原因为: " + cause);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常,请稍后重试");
        }
    }


    /**
     * 在消息无法被路由到任何队列时执行
     *
     * @param returned the returned message and metadata.
     */
    @Override
    public void returnedMessage(ReturnedMessage returned) {
        Message message = returned.getMessage();
        log.info("消息失败的回退id " + message);
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常,请稍后重试");
    }
}
