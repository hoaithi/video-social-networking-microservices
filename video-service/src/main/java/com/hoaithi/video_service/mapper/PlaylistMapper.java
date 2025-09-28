package com.hoaithi.video_service.mapper;

import com.hoaithi.video_service.dto.response.PlaylistResponse;
import com.hoaithi.video_service.entity.Playlist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {
    PlaylistResponse toPlaylistResponse(Playlist playlist);
}
