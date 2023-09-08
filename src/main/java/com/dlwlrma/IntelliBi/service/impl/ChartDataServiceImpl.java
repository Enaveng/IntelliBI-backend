package com.dlwlrma.IntelliBi.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlwlrma.IntelliBi.common.ErrorCode;
import com.dlwlrma.IntelliBi.exception.BusinessException;
import com.dlwlrma.IntelliBi.model.entity.ChartData;
import com.dlwlrma.IntelliBi.mapper.ChartDataMapper;
import com.dlwlrma.IntelliBi.service.ChartDataService;
import org.springframework.stereotype.Service;

@Service
public class ChartDataServiceImpl extends ServiceImpl<ChartDataMapper, ChartData> implements ChartDataService {
    @Override
    public boolean saveChartData(Long id, String cvsData) {
        //插入图表原始数据插入到chart_data数据库当中
        ChartData chartData = new ChartData();
        chartData.setChartId(id);
        chartData.setChartData(cvsData);
        boolean result = this.save(chartData);
        if (result) {
            return true;
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存信息失败");
        }

    }

    @Override
    public String getDataByChartId(long chartId) {
        QueryWrapper<ChartData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("chartId", chartId);
        ChartData chartData = this.getOne(queryWrapper);
        return chartData.getChartData();
    }
}
