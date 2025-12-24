package com.hoaithi.comment_service.controller;

import com.hoaithi.comment_service.dto.request.CommentRequest;
import com.hoaithi.comment_service.dto.response.ApiResponse;
import com.hoaithi.comment_service.dto.response.CommentCountResponse;
import com.hoaithi.comment_service.dto.response.CommentResponse;
import com.hoaithi.comment_service.service.CommentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/comment")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {
    CommentService commentService;

    @PostMapping
    public ApiResponse<CommentResponse> createComment(@RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createComment(request))
                .message("Created Comment successfully")
                .build();
    }

    @GetMapping("/{itemId}")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable String itemId) {
        return ApiResponse.<List<CommentResponse>>builder()
                .result(commentService.getComments(itemId))
                .message("Retrieved comments successfully")
                .build();
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable String commentId,
            @RequestBody CommentRequest request){
        return ApiResponse.<CommentResponse>builder()
                .message("Updated comment successfully")
                .result(commentService.updateComment(commentId, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteComment(@PathVariable String id){
        commentService.deleteComment(id);
        return ApiResponse.<String>builder()
                .result("Deleted comment successfully")
                .build();
    }

    @PostMapping("/{commentId}/heart")
    public ApiResponse<CommentResponse> heartComment(@PathVariable String commentId){
        return ApiResponse.<CommentResponse>builder()
                .message("Updated comment successfully")
                .result(commentService.heartComment(commentId))
                .build();
    }

    @GetMapping("/internal/{itemId}/count")
    public ApiResponse<CommentCountResponse> countComment(@PathVariable String itemId){
        return ApiResponse.<CommentCountResponse>builder()
                .result(commentService.getCommentCount(itemId))
                .build();
    }
    @GetMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentReplies(@PathVariable String id) {
        List<CommentResponse> replies = commentService.getRepliesByCommentId(id);

        return ResponseEntity.ok(ApiResponse.<List<CommentResponse>>builder()
                .message("Replies retrieved successfully")
                .result(replies)
                .build());
    }

    @PostMapping("/{id}/replies")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CommentResponse>> replyToComment(
            @PathVariable String id,
            @Valid @RequestBody CommentRequest request) {

        request.setParentCommentId(id);

        CommentResponse reply = commentService.createComment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CommentResponse>builder()
                        .message("Reply added successfully")
                        .result(reply)
                        .build());
    }


}