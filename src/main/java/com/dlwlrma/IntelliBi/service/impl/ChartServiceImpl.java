package com.dlwlrma.IntelliBi.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlwlrma.IntelliBi.BiRabbitMq.BiMessageProducer;
import com.dlwlrma.IntelliBi.mapper.ChartMapper;
import com.dlwlrma.IntelliBi.service.ChartService;
import com.dlwlrma.IntelliBi.common.ErrorCode;
import com.dlwlrma.IntelliBi.exception.BusinessException;
import com.dlwlrma.IntelliBi.exception.ThrowUtils;
import com.dlwlrma.IntelliBi.manager.AiManager;
import com.dlwlrma.IntelliBi.manager.RedisLimiterManager;
import com.dlwlrma.IntelliBi.model.dto.chart.GenChartByAiRequest;
import com.dlwlrma.IntelliBi.model.entity.Chart;
import com.dlwlrma.IntelliBi.model.entity.ChartData;
import com.dlwlrma.IntelliBi.model.entity.User;
import com.dlwlrma.IntelliBi.model.enums.ChartStatusEnum;
import com.dlwlrma.IntelliBi.model.vo.BiResponseVo;
import com.dlwlrma.IntelliBi.model.vo.ChartDataVo;
import com.dlwlrma.IntelliBi.service.AiFrequencyService;
import com.dlwlrma.IntelliBi.service.ChartDataService;
import com.dlwlrma.IntelliBi.service.UserService;
import com.dlwlrma.IntelliBi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 86158
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2023-08-23 19:01:33
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private UserService userService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private ChartDataService chartDataService;


    @Override
    public BiResponseVo getAnswerByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {

        String name = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //得到当前登录用户以及id信息
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        //查询当前用户是否有AI调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(userId);
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI调用剩余次数不足！");
        }

        //对用户的请求操作进行限流    表示对同一个用户调用相同接口进行限流 表示的是同一个用户每秒只能请求一次请求
        redisLimiterManager.doRateLimit("genChartByAi_" + userId);

        //完善用户的输入 让我们得到AI模型更好的结果
        StringBuffer userInput = new StringBuffer();
        userInput.append("分析需求: ").append("\n");
        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ", 请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据: ").append("\n");
        //得到压缩之后的数据 excel -> csv
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        //调用API接口
        Long BIModelId = 1695303741311295489L;
        String result = aiManager.doChat(BIModelId, userInput.toString());
        //AI返回结果拆分
        int indexOf = result.lastIndexOf("}");
        String genChart = result.substring(0, indexOf + 1);
        String genResult = result.substring(indexOf + 1);
        System.out.println(genChart);
        System.out.println(genResult);
        //插入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setChartType(chartType);
        chart.setGoal(goal);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(userId);
        chart.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
        boolean save = this.save(chart);
        //插入图表原始数据插入到chart_data数据库当中
        Long ChartId = chart.getId();
        chartDataService.saveChartData(ChartId, csvData);

        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "图表信息保存失败");
        BiResponseVo biResponseVo = new BiResponseVo();
        biResponseVo.setGenChart(genChart);
        biResponseVo.setGenResult(genResult);

        //将用户的调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        return biResponseVo;
    }

    @Override
    public BiResponseVo getAnswerAsyncByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //得到当前登录用户以及id信息
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        //查询当前用户是否有AI调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(userId);
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI调用剩余次数不足！");
        }

        //对用户的请求操作进行限流    表示对同一个用户调用相同接口进行限流 表示的是同一个用户每秒只能请求一次请求
        redisLimiterManager.doRateLimit("genChartByAi_" + userId);

        //完善用户的输入 让我们得到AI模型更好的结果
        StringBuffer userInput = new StringBuffer();
        userInput.append("分析需求: ").append("\n");
        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ", 请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据: ").append("\n");
        //得到压缩之后的数据 excel -> csv
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //异步任务时在调用AI接口之前先将用户提交的请求任务保存到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(userId);
        //插入图表原始数据插入到chart_data数据库当中
        Long ChartId = chart.getId();
        chartDataService.saveChartData(ChartId, csvData);
        boolean saveResult = this.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //异步实现调用AI接口任务
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
            // 等待-->执行中--> 成功/失败
            // 更新用户请求任务的状态
            Chart updateChart = new Chart();
            updateChart.setChartStatus(ChartStatusEnum.RUNNING.getValue());
            updateChart.setId(chart.getId());
            boolean updateResult = this.updateById(updateChart);
            if (!updateResult) {
                Chart failChart = new Chart();
                failChart.setId(chart.getId());
                failChart.setChartStatus(ChartStatusEnum.FAILED.getValue()); //状态更改为错误状态
                failChart.setExecMessage("更新图表·执行中状态·失败");
                this.updateById(failChart);
            }
            //调用API接口
            Long BIModelId = 1695303741311295489L;
            String result = aiManager.doChat(BIModelId, userInput.toString());
            //AI返回结果拆分
            int indexOf = result.lastIndexOf("}");
            String genChart = result.substring(0, indexOf + 1);
            String genResult = result.substring(indexOf + 1);

            // 生成的最终结果
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setChartStatus(ChartStatusEnum.SUCCEED.getValue());
            boolean updateResult2 = this.updateById(updateChartResult);
            if (!updateResult2) {
                Chart updateChartFailed = new Chart();
                updateChartFailed.setId(chart.getId());
                updateChartFailed.setChartStatus(ChartStatusEnum.FAILED.getValue());
                updateChartFailed.setExecMessage("更新图表·成功状态·失败");
                this.updateById(updateChartFailed);
            }
        }, threadPoolExecutor);

        BiResponseVo biResponseVo = new BiResponseVo();
        Chart finalChart = this.getById(chart.getId());
        String genChart = finalChart.getGenChart();
        String genResult = finalChart.getGenResult();
        biResponseVo.setGenChart(genChart);
        biResponseVo.setGenResult(genResult);

        //将用户的调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        return biResponseVo;
    }

    @Override
    public BiResponseVo getAnswerAsyncByAiMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getChartName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //得到当前登录用户以及id信息
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();

        //查询当前用户是否有AI调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(userId);
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI调用剩余次数不足！");
        }

        //对用户的请求操作进行限流    表示对同一个用户调用相同接口进行限流 表示的是同一个用户每秒只能请求一次请求
        redisLimiterManager.doRateLimit("genChartByAi_" + userId);

        //完善用户的输入 让我们得到AI模型更好的结果
        StringBuffer userInput = new StringBuffer();
        userInput.append("分析需求: ").append("\n");
        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += ", 请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据: ").append("\n");
        //得到压缩之后的数据 excel -> csv
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");

        //在调用AI接口之前先将用户提交的请求任务保存到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        //chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setChartStatus(ChartStatusEnum.WAIT.getValue());
        chart.setUserId(userId);
        boolean saveResult = this.save(chart);
        //插入图表原始数据插入到chart_data数据库当中
        Long ChartId = chart.getId();
        chartDataService.saveChartData(ChartId, csvData);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //获取当前请求的数据库id
        Long chartId = chart.getId();
        //保存数据库成功之后采用rabbitmq发送请求参数到消息队列当中
        biMessageProducer.sendMessage(String.valueOf(chartId));

        BiResponseVo biResponseVo = new BiResponseVo();
        Chart finalChart = this.getById(chart.getId());
        String genChart = finalChart.getGenChart();
        String genResult = finalChart.getGenResult();
//        if (StringUtils.isBlank(genChart) || StringUtils.isBlank(genResult)) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "调用失败");
//        }
        biResponseVo.setGenChart(genChart);
        biResponseVo.setGenResult(genResult);

        //将用户的调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        return biResponseVo;
    }

    @Override
    public ChartDataVo getDataById(long id) {
        //根据id查询原数据信息
        QueryWrapper<ChartData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ChartId", id);
        ChartData chartData = chartDataService.getOne(queryWrapper);
        String chartDataResult = chartData.getChartData();
        if (chartDataResult == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图表id信息不存在");
        }
        //根据id查询其他参数
        QueryWrapper<Chart> chartQueryWrapper = new QueryWrapper<>();
        chartQueryWrapper.select("name", "genChart", "genResult", "goal").eq("id", id);
        Chart chart = this.getOne(chartQueryWrapper);
        ChartDataVo chartDataVo = new ChartDataVo();
        BeanUtils.copyProperties(chart, chartDataVo);
        chartDataVo.setChartData(chartDataResult);
        return chartDataVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeChartById(long id) {
        QueryWrapper<ChartData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chartId", id);
        chartDataService.remove(queryWrapper);
        return this.removeById(id);
    }
}




