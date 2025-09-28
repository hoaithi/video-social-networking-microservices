package com.hoaithi.video_service.service;

import com.hoaithi.video_service.dto.request.PlaylistCreationRequest;
import com.hoaithi.video_service.dto.response.PlaylistResponse;
import com.hoaithi.video_service.entity.Playlist;
import com.hoaithi.video_service.mapper.PlaylistMapper;
import com.hoaithi.video_service.repository.PlaylistRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlaylistService {
    PlaylistRepository playlistRepository;
    PlaylistMapper playlistMapper;

    public List<PlaylistResponse> getPlaylists(String profileId){
        List<Playlist> playlists = playlistRepository.findAllByProfileId(profileId);
        return playlists.stream()
                .map(playlist -> {
                    PlaylistResponse response = playlistMapper.toPlaylistResponse(playlist);
                    response.setVideoCount(playlist.getVideoPlaylists().size());
                    return response;
                })
                .toList();

    }
    public List<PlaylistResponse> getMyPlaylists(){
        String currentProfileId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Playlist> playlists = playlistRepository.findAllByProfileId(currentProfileId);
        return playlists.stream()
                .map(playlist -> {
                    PlaylistResponse response = playlistMapper.toPlaylistResponse(playlist);
                    response.setVideoCount(playlist.getVideoPlaylists().size());
                    return response;
                })
                .toList();

    }
    public PlaylistResponse createPlaylist(PlaylistCreationRequest request){
        String currentProfileId = SecurityContextHolder.getContext().getAuthentication().getName();
        Playlist playlist = Playlist.builder()
                .name(request.getName())
                .profileId(currentProfileId)
                .build();
        var savedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toPlaylistResponse(savedPlaylist);
    }
}
