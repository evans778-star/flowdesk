package com.aiwork.helper.entity.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 补卡信息 (嵌入文档)
 */
@Data
public class MakeCard {

    /**
     * 补卡时间
     */
    @Field("date")
    private Long date;

    /**
     * 补卡理由
     */
    @Field("reason")
    private String reason;

    /**
     * 补卡日期 (格式: 20221011)
     */
    @Field("day")
    private Long day;

    /**
     * 补卡类型 (1-上班卡，2-下班卡)
     */
    @Field("workCheckType")
    private Integer checkType;
}
