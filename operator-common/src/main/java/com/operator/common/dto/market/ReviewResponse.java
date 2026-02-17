package com.operator.common.dto.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Review Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long marketItemId;
    private Long userId;
    private String userName;
    private String content;
    private Integer rating;
    private Integer likesCount;
    private Long parentId;
    private List<ReviewResponse> replies;
    private LocalDateTime createdAt;
}
