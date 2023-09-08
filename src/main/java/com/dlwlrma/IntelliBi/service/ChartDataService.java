package com.dlwlrma.IntelliBi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.dlwlrma.IntelliBi.model.entity.ChartData;
import org.springframework.stereotype.Service;

@Service
public interface ChartDataService extends IService<ChartData> {
    boolean saveChartData(Long id,String cvsData);

    String getDataByChartId(long chartId);
}
