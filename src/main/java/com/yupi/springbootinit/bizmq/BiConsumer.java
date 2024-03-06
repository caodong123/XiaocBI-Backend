package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.AIUtil;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Component
public class BiConsumer {
    @Resource
    private ChartService chartService;

    /**
     *
     * @param message 接收的消息  chart的id
     * @param channel 连接
     * @param deliverTag 消息的id
     * @throws IOException
     */
    //queues指定监听的队列
    @RabbitListener(queues = {BiConstant.QUEUE_NAME},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliverTag) throws IOException {
        //message是id
        String chartId=message;
        Chart chart = chartService.getById(chartId);

        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean b = chartService.updateById(updateChart);
        if (!b) {
            handleChartUpdateError(chart.getId(), "更新图表执行中信息失败");
            return;
        }
        //调用AI
        String response = null;
        String userInput=buildUserInput(chart);
        try {
            AIUtil aiUtil = new AIUtil();
            response = aiUtil.doChat(userInput.toString());
        } catch (Exception e) {
            handleChartUpdateError(chart.getId(), "Ai生成错误");
        }
        //提取结果
        String[] splits = new String[0];
        if (!response.contains("【【【【【")) {
            handleChartUpdateError(chart.getId(), "ai生成出现错误");
        }
        splits = response.split("【【【【【");
        String temp = splits[1].trim();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < temp.length(); i++) {
            if (temp.charAt(i) != '\'') res.append(temp.charAt(i));
            else res.append("\"");
        }
        String chartCode = null;
        String analyseResult = null;
        if (!res.toString().contains("】】】】")) {
            handleChartUpdateError(chart.getId(), "ai生成出现错误");
        }

        chartCode = res.toString().split("】】】】")[0];
        analyseResult = res.toString().split("】】】】")[1];
        //再更新一次
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setStatus("succeed");
        updateChartResult.setGenChart(chartCode);
        updateChartResult.setGenResult(analyseResult);
        boolean b1 = chartService.updateById(updateChartResult);
        if (!b1) {
            handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
        }
        log.info("收到消息:"+message);
        //手动确认ack
        channel.basicAck(deliverTag,false);
    }


    private String buildUserInput(Chart chart){
        StringBuffer userInput=new StringBuffer();
        String goal=chart.getGoal();
        String chartType= chart.getChartType();
        String data=chart.getChartData();

        //目标
        userInput.append("分析需求：").append("\n");
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        //原始数据
        //处理excel
        userInput.append("原始数据：").append("\n");
        userInput.append(data);

        return userInput.toString();
    }


    private void handleChartUpdateError(long chartId, String message) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus("failed");
        chart.setExecMessage(message);
        boolean updateById = chartService.updateById(chart);
        if (!updateById) {
            log.info("更新图表失败状态失败,id:" + chartId);
        }


    }

}
