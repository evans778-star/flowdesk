package com.aiwork.helper.entity.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 外出信息 (嵌入文档)
 */
@Data
public class GoOut {

    /**
     * 开始时间
     */
    @Field("startTime")
    private Long startTime;

    /**
     * 结束时间
     */
    @Field("endTime")
    private Long endTime;

    /**
     * 时长 (小时)
     */
    @Field("duration")
    private Float duration;

    /**
     * 外出原因
     */
    @Field("reason")
    private String reason;
}