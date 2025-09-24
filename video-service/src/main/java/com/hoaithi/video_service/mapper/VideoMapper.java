package com.hoaithi.video_service.mapper;

import com.hoaithi.video_service.dto.request.VideoCreationRequest;
import com.hoaithi.video_service.dto.response.VideoResponse;
import com.hoaithi.video_service.entity.Video;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VideoMapper {
    Video toVideo(VideoCreationRequest request);

    VideoResponse toVideoResponse(Video video);
}
