package com.hoaithi.identity_service.mapper;


import com.hoaithi.identity_service.dto.request.PermissionRequest;
import com.hoaithi.identity_service.dto.response.PermissionResponse;
import com.hoaithi.identity_service.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
