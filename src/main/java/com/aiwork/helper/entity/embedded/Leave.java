package com.aiwork.helper.entity.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 请假信息 (嵌入文档)
 */
@Data
public class Leave {

    /**
     * 请假类型 (1-事假, 2-调休, 3-病假, 4-年假, 5-产假, 6-陪产假, 7-婚假, 8-丧假, 9-哺乳假)
     */
    @Field("type")
    private Integer type;

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
     * 请假原因
     */
    @Field("reason")
    private String reason;

    /**
     * 请假类型 (1-小时，2-天)
     */
    @Field("timeType")
    private Integer timeType;

    /**
     * 请假时长 (根据timeType计算，单位：小时或天)
     */
    @Field("duration")
    private Float duration;
}