package com.dlwlrma.IntelliBi.BiRabbitMq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dlwlrma.IntelliBi.common.ErrorCode;
import com.dlwlrma.IntelliBi.config.RabbitMqConfig;
import com.dlwlrma.IntelliBi.exception.BusinessException;
import com.dlwlrma.IntelliBi.model.entity.Chart;
import com.dlwlrma.IntelliBi.model.enums.ChartStatusEnum;
import com.dlwlrma.IntelliBi.service.ChartService;
import com.rabbitmq.client.Channel;
import com.dlwlrma.IntelliBi.model.entity.AiFrequency;
import com.dlwlrma.IntelliBi.service.AiFrequencyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;


/**
 * 监听死信队列
 */
@Slf4j
@Component
public class BiMessageDeadConsumer {

    @Resource
    private ChartService chartService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @RabbitListener(queues = RabbitMqConfig.BI_DEAD_QUEUE_NAME)
    public void receiveDeadMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("接收死信队列的消息为:" + message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表不存在");
        }
        // 把图表标为失败
        chart.setChartStatus(ChartStatusEnum.FAILED.getValue());
        boolean updateById = chartService.updateById(chart);
        if (!updateById) {
            log.info("处理死信队列消息失败,失败图表id:{}", chart.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 把调用次数补偿给用户
        Long userId = chart.getUserId();
        QueryWrapper<AiFrequency> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        AiFrequency frequency = aiFrequencyService.getOne(wrapper);
        Integer remainFrequency = frequency.getRemainFrequency();
        frequency.setRemainFrequency(remainFrequency + 1);
        boolean result = aiFrequencyService.updateById(frequency);
        if (!result) {
            log.info("处理死信队列 补偿用户次数 消息失败,失败用户id:{}", userId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 确认消息
        channel.basicAck(deliveryTag, false);
    }
}
