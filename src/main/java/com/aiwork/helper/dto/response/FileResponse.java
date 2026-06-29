package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {

    /**
     * 文件访问主机地址
     */
    private String host;

    /**
     * 文件相对路径
     */
    private String file;

    /**
     * 文件名称
     */
    private String filename;
}
