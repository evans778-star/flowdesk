package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitationResponse {

    private String documentId;

    private String documentName;

    private Integer chunkId;

    private Integer pageNumber;

    private Double score;

    private String snippet;
}
