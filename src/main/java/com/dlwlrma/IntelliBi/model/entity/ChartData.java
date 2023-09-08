package com.dlwlrma.IntelliBi.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChartData implements Serializable {


    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    /**
     * 图表信息
     */
    private String ChartData;


    /**
     * 图表id
     */
    private Long ChartId;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
