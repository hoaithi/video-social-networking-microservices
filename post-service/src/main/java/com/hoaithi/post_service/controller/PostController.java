package com.hoaithi.post_service.controller;

import com.hoaithi.post_service.dto.request.CreationPostRequest;
import com.hoaithi.post_service.dto.response.ApiResponse;
import com.hoaithi.post_service.dto.response.PagedResponse;
import com.hoaithi.post_service.dto.response.PostResponse;
import com.hoaithi.post_service.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/post")
public class PostController {
    PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(
            @RequestPart(name = "image", required = false) MultipartFile image,
            @RequestPart(name = "title") String title,
            @RequestPart(name = "content", required = false) String content

    ) {
        CreationPostRequest post = CreationPostRequest.builder()
                .content(content)
                .title(title)
                .build();
        PostResponse response = postService.createPost(post, image);
        return ApiResponse.<PostResponse>builder()
                .message("Post created successfully")
                .result(response)
                .build();
    }

    @GetMapping("/my-posts")
    public ApiResponse<?> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.<PagedResponse<PostResponse>>builder()
                .message("Retrieved my posts successfully")
                .result(postService.getMyPosts(page, size))
                .build();
    }


    @GetMapping("/{userId}")
    public ApiResponse<?> getPostByUserId(@PathVariable String userId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PagedResponse<PostResponse>>builder()
                .message("Retrieved posts by user ID successfully")
                .result(postService.getPostByProfileId(userId, page, size))
                .build();
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @PathVariable String postId,
            @RequestPart(value = "post", required = false) CreationPostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        PostResponse response = postService.updatePost(postId, request, image);
        return ApiResponse.<PostResponse>builder()
                .message("Post updated successfully")
                .result(response)
                .build();
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable String postId) {
        postService.deletePost(postId);
        return ApiResponse.<Void>builder()
                .message("Post deleted successfully")
                .result(null)
                .build();
    }


}
