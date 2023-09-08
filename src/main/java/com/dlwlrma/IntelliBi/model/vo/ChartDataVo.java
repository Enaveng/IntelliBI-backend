package com.dlwlrma.IntelliBi.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChartDataVo implements Serializable {

    /**
     * 图表原数据
     */
    private String chartData;

    /**
     * 图表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 生成的图表信息
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
