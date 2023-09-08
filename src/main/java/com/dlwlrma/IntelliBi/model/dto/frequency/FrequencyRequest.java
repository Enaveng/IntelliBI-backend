package com.dlwlrma.IntelliBi.model.dto.frequency;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dlwlrma
 * 使用次数
 */
@Data
public class FrequencyRequest implements Serializable {
    private int frequency;
}
