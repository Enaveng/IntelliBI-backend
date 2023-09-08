package com.dlwlrma.IntelliBi.BiRabbitMq;


import com.dlwlrma.IntelliBi.config.RabbitMqConfig;
import com.dlwlrma.IntelliBi.service.ChartDataService;
import com.rabbitmq.client.Channel;
import com.dlwlrma.IntelliBi.common.ErrorCode;
import com.dlwlrma.IntelliBi.exception.BusinessException;
import com.dlwlrma.IntelliBi.manager.AiManager;
import com.dlwlrma.IntelliBi.model.entity.Chart;
import com.dlwlrma.IntelliBi.model.enums.ChartStatusEnum;
import com.dlwlrma.IntelliBi.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;


/**
 *  监听正常得到队列消息的消费者方法
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private AiManager aiManager;
    @Resource
    private ChartService chartService;
    @Resource
    private ChartDataService chartDataService;

    @RabbitListener(queues = RabbitMqConfig.BI_QUEUE_NAME)
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("接收到的消息为:" + message);
        //校验
        if (!StringUtils.isNotBlank(message)) {
            //表示接收的消息参数为空 将该消息拒绝
            channel.basicNack(deliveryTag, false, false);  //表示单个消息拒绝 不将拒绝的消息放回队列当中
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接受到的消息为空");
        }
        //得到消息传递的chartId(传递的是string类型)
        long chartId = Long.parseLong(message);
        // 等待-->执行中--> 成功/失败
        // 更新用户请求任务的状态
        Chart updateChart = new Chart();
        updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
        updateChart.setId(chartId);
        boolean updateResult = chartService.updateById(updateChart);
        if (!updateResult) {
            //将该条消息拒绝并更改数据库状态
            channel.basicNack(deliveryTag, false, false);
            Chart failChart = new Chart();
            failChart.setId(chartId);
            failChart.setChartStatus(ChartStatusEnum.FAILED.getValue()); //状态更改为错误状态
            failChart.setExecMessage("更新图表·执行中状态·失败");
            chartService.updateById(failChart);
        }
        //调用API接口
        Long BIModelId = 1695303741311295489L;
        String result = aiManager.doChat(BIModelId, this.buildInput(chartId));

        if (StringUtils.isBlank(result)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI调用失败");
        }

        //AI返回结果拆分
        int indexOf = result.lastIndexOf("}");
        String genChart = result.substring(0, indexOf + 1);
        String genResult = result.substring(indexOf + 1);

        // 生成的最终结果
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        //将请求状态更改为成功
        updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean updateResult2 = chartService.updateById(updateChartResult);
        if (!updateResult2) {
            channel.basicNack(deliveryTag, false, true);
            Chart updateChartFailed = new Chart();
            updateChartFailed.setId(chartId);
            updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
            updateChartFailed.setExecMessage("更新图表·成功状态·失败");
            chartService.updateById(updateChartFailed);
        }
        //消息确认
        channel.basicAck(deliveryTag, false);
    }


    //方法构建用户的输入
    public String buildInput(long chartId) {
        //根据id查询chart
        Chart chart = chartService.getById(chartId);
        String goal = chart.getGoal();
        String chartData = chartDataService.getDataByChartId(chartId);
        String chartType = chart.getChartType();
        //完善用户的输入 让我们得到AI模型更好的结果
        StringBuffer userInput = new StringBuffer();
        userInput.append("分析需求: ").append("\n");
        //拼接分析目标
        if (StringUtils.isNotBlank(chartType)) {
            goal += ", 请使用" + chartType;
        }
        userInput.append(goal).append("\n");
        userInput.append("原始数据: ").append("\n");
        //得到压缩之后的数据 excel -> csv
        userInput.append(chartData).append("\n");
        return userInput.toString();
    }

}
