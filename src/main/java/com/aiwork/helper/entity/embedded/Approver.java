package com.aiwork.helper.entity.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 审批人 (嵌入文档)
 */
@Data
public class Approver {

    /**
     * 用户ID
     */
    @Field("userId")
    private String userId;

    /**
     * 用户姓名
     */
    @Field("userName")
    private String userName;

    /**
     * 审批状态
     */
    @Field("status")
    private Integer status;

    /**
     * 审批理由
     */
    @Field("reason")
    private String reason;
}
