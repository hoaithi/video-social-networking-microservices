package com.hoaithi.post_service.service;

import com.hoaithi.post_service.dto.request.CreationPostRequest;
import com.hoaithi.post_service.dto.response.OwnerIdResponse;
import com.hoaithi.post_service.dto.response.PagedResponse;
import com.hoaithi.post_service.dto.response.PostResponse;
import com.hoaithi.post_service.dto.response.ProfileResponse;
import com.hoaithi.post_service.entity.Post;
import com.hoaithi.post_service.exception.AppException;
import com.hoaithi.post_service.exception.ErrorCode;
import com.hoaithi.post_service.mapper.PostMapper;
import com.hoaithi.post_service.repository.PostRepository;
import com.hoaithi.post_service.repository.httpclient.FileClient;
import com.hoaithi.post_service.repository.httpclient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    ProfileClient profileClient;
    /**
     * Creates a new post.
     *
     * @param request the request containing post details
     * @return the response containing created post details
     */
    public PostResponse createPost(CreationPostRequest request, MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Post post = Post.builder()
                .profileId(authentication.getName())
                .content(request.getContent())
                .title(request.getTitle())
                .imageUrl(fileClient.uploadFile(image))
                .build();
        return postMapper.toCreationPostResponse(postRepository.save(post));
    }


    public PagedResponse<PostResponse> getMyPosts(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findByProfileId(authentication.getName(), pageable);
        Page<PostResponse> responses = posts.map(postMapper::toCreationPostResponse);
        return PagedResponse.<PostResponse>builder()
                .content(responses.getContent())
                .page(responses.getNumber())
                .size(responses.getSize())
                .totalElements(responses.getTotalElements())
                .totalPages(responses.getTotalPages())
                .last(responses.isLast())
                .build();
    }


    public PagedResponse<PostResponse> getPostByProfileId(String profileId, int page, int size) {

        if (profileId == null || profileId.isBlank()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> posts = postRepository.findByProfileId(profileId, pageable);
        Page<PostResponse> responses = posts.map((post) -> {
            PostResponse postResponse = postMapper.toCreationPostResponse(post);
            ProfileResponse profileResponse = profileClient.getProfileById(postResponse.getProfileId()).getResult();
            postResponse.setProfileImage(profileResponse.getAvatarUrl());
            postResponse.setProfileName(profileResponse.getFullName());
            return postResponse;
        });
        return PagedResponse.<PostResponse>builder()
                .content(responses.getContent())
                .page(responses.getNumber())
                .size(responses.getSize())
                .totalElements(responses.getTotalElements())
                .totalPages(responses.getTotalPages())
                .last(responses.isLast())
                .build();
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
    public OwnerIdResponse getOwnerId(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        return OwnerIdResponse.builder()
                .ownerId(post.getProfileId())
                .build();
    }

}
