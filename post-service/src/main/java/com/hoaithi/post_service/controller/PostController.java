package com.hoaithi.post_service.controller;

import com.hoaithi.post_service.dto.request.CreationPostRequest;
import com.hoaithi.post_service.dto.response.ApiResponse;
import com.hoaithi.post_service.dto.response.PostResponse;
import com.hoaithi.post_service.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
    PostService postService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(
            @RequestPart(value = "post", required = false) CreationPostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image)
    {
        PostResponse response = postService.createPost(request, image);
        return ApiResponse.<PostResponse>builder()
                .message("Post created successfully")
                .result(response)
                .build();
    }

    @GetMapping("/my-posts")
    public ApiResponse<?> getMyPosts() {
        return ApiResponse.<List<PostResponse>>builder()
                .message("Retrieved my posts successfully")
                .result(postService.getMyPosts())
                .build();
    }
}
