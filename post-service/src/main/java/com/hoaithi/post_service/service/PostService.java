package com.hoaithi.post_service.service;

import com.hoaithi.post_service.dto.request.CreationPostRequest;
import com.hoaithi.post_service.dto.response.PostResponse;
import com.hoaithi.post_service.entity.Post;
import com.hoaithi.post_service.exception.AppException;
import com.hoaithi.post_service.exception.ErrorCode;
import com.hoaithi.post_service.mapper.PostMapper;
import com.hoaithi.post_service.repository.PostRepository;
import com.hoaithi.post_service.repository.httpclient.FileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {
    PostRepository postRepository;
    PostMapper postMapper;
    FileClient fileClient;
    /**
     * Creates a new post.
     *
     * @param request the request containing post details
     * @return the response containing created post details
     */
    public PostResponse createPost(CreationPostRequest request, MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Simulate post creation logic
        Post post = Post.builder()
                .profileId(authentication.getName())
                .content(request.getContent())
                .imageUrl(fileClient.uploadFile(image))
                .build();
        return postMapper.toCreationPostResponse(postRepository.save(post));
    }


    public List<PostResponse> getMyPosts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Post> posts = postRepository.findByProfileId(authentication.getName());
        return posts.stream().map(postMapper::toCreationPostResponse).toList();
    }


    public List<PostResponse> getPostByProfileId(String profileId) {

        if (profileId == null || profileId.isBlank()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        List<Post> posts = postRepository.findByProfileId(profileId);
        return posts.stream().map(postMapper::toCreationPostResponse).toList();
    }

    public PostResponse updatePost(String postId, CreationPostRequest request, MultipartFile image) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        if(!isPostOwner(post.getProfileId()))
            throw new AppException(ErrorCode.UNAUTHORIZED);
        if (request.getContent() != null) {
            post.setContent(request.getContent());
        }

        if (image != null && !image.isEmpty()) {
            post.setImageUrl(fileClient.uploadFile(image));
        }

        return postMapper.toCreationPostResponse(postRepository.save(post));
    }

    public void deletePost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        if (isPostOwner(post.getProfileId())){
            postRepository.delete(post);
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private boolean isPostOwner(String ownerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return ownerId.equals(userId);
    }

}
