package com.hoaithi.post_service.controller;

import com.hoaithi.post_service.dto.response.OwnerIdResponse;
import com.hoaithi.post_service.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalPostController {
    PostService postService;
    @GetMapping("/internal/post/{postId}")
    public OwnerIdResponse getOwnerId(@PathVariable String postId){
        return postService.getOwnerId(postId);
    }
}
