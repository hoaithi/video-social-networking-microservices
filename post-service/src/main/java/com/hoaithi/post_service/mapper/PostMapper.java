package com.hoaithi.post_service.mapper;

import com.hoaithi.post_service.dto.request.CreationPostRequest;
import com.hoaithi.post_service.dto.response.PostResponse;
import com.hoaithi.post_service.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {
    Post toPost(CreationPostRequest request);
    PostResponse toCreationPostResponse(Post post);
}
