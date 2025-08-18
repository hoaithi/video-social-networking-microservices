package com.hoaithi.profile_service.mapper;


import com.hoaithi.profile_service.dto.request.ProfileRequest;
import com.hoaithi.profile_service.dto.response.ProfileResponse;
import com.hoaithi.profile_service.dto.response.UpdateProfileResponse;
import com.hoaithi.profile_service.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    Profile toProfile(ProfileRequest profileRequest);

    ProfileResponse toProfileResponse(Profile profile);
    UpdateProfileResponse toUpdateProfileResponse(Profile profile);
}