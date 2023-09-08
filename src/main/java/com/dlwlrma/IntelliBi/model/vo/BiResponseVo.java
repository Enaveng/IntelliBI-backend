package com.dlwlrma.IntelliBi.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BiResponseVo implements Serializable {
    /**
     * 生成的图表信息
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    private static final long serialVersionUID = 1L;
}
