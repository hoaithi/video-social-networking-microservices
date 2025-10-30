package com.hoaithi.identity_service.service;

import com.hoaithi.event.dto.CreationUserEvent;
import com.hoaithi.identity_service.constant.PredefinedRole;
import com.hoaithi.identity_service.dto.request.ProfileRequest;
import com.hoaithi.identity_service.dto.request.SavePasswordRequest;
import com.hoaithi.identity_service.dto.request.UserCreationRequest;
import com.hoaithi.identity_service.dto.request.UserUpdateRequest;
import com.hoaithi.identity_service.dto.response.UserResponse;
import com.hoaithi.identity_service.entity.Role;
import com.hoaithi.identity_service.entity.User;
import com.hoaithi.identity_service.exception.AppException;
import com.hoaithi.identity_service.exception.ErrorCode;
import com.hoaithi.identity_service.mapper.UserMapper;
import com.hoaithi.identity_service.repository.RoleRepository;
import com.hoaithi.identity_service.repository.UserRepository;
import com.hoaithi.identity_service.repository.httpclient.ProfileClient;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USER_EXISTED);
        if (userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.EMAIL_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));


        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        user.setEmail(request.getEmail());
        user = userRepository.save(user);
        ProfileRequest profileRequest = ProfileRequest.builder()
                .fullName(request.getFullName())
                .city(request.getCity())
                .dob(request.getDob())
                .userId(user.getId())
                .email(request.getEmail())
                .hasPassword(true)
                .build();

        Object profile = profileClient.createProfile(profileRequest);

        // publish event to kafka
        kafkaTemplate.send("user-topic", CreationUserEvent.builder()
                .email(request.getEmail())
                .build());
        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

}
