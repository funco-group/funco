package com.found_404.funco.note.dto.response;

import com.found_404.funco.feignClient.dto.SimpleMember;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CommentsResponse(
    Long commentId,
    SimpleMember member,
    List<CommentsResponse> childComments,
    String content,
    LocalDateTime date

) {
}