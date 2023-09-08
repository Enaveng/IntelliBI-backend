package com.dlwlrma.IntelliBi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dlwlrma.IntelliBi.model.dto.chart.GenChartByAiRequest;
import com.dlwlrma.IntelliBi.model.entity.Chart;
import com.dlwlrma.IntelliBi.model.vo.BiResponseVo;
import com.dlwlrma.IntelliBi.model.vo.ChartDataVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 86158
 * @description 针对表【chart(图表信息表)】的数据库操作Service
 * @createDate 2023-08-23 19:01:33
 */
public interface ChartService extends IService<Chart> {

    //同步任务方式实现
    BiResponseVo getAnswerByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    //异步任务方式实现
    BiResponseVo getAnswerAsyncByAi(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    BiResponseVo getAnswerAsyncByAiMq(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, HttpServletRequest request);

    ChartDataVo getDataById(long id);

    boolean removeChartById(long id);
}
