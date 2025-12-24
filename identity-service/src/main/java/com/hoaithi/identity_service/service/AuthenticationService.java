package com.hoaithi.identity_service.service;


import com.hoaithi.event.dto.CreationUserEvent;
import com.hoaithi.event.dto.ForgetPasswordEvent;
import com.hoaithi.identity_service.constant.PredefinedRole;
import com.hoaithi.identity_service.dto.request.*;
import com.hoaithi.identity_service.dto.response.*;
import com.hoaithi.identity_service.entity.InvalidatedToken;
import com.hoaithi.identity_service.entity.Role;
import com.hoaithi.identity_service.entity.User;
import com.hoaithi.identity_service.exception.AppException;
import com.hoaithi.identity_service.exception.ErrorCode;
import com.hoaithi.identity_service.mapper.UserMapper;
import com.hoaithi.identity_service.repository.InvalidatedTokenRepository;
import com.hoaithi.identity_service.repository.RoleRepository;
import com.hoaithi.identity_service.repository.UserRepository;
import com.hoaithi.identity_service.repository.httpclient.GoogleClient;
import com.hoaithi.identity_service.repository.httpclient.GoogleUserInfoClient;
import com.hoaithi.identity_service.repository.httpclient.ProfileClient;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    KafkaTemplate<String, Object> kafkaTemplate;
    RedisTemplate<String, String> redisTemplate;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    GoogleClient googleClient;
    GoogleUserInfoClient googleUserInfoClient;
    RoleRepository roleRepository;
    private UserMapper userMapper;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String signerKey;

    @NonFinal
    @Value("${google.client-id}")
    String clientId;

    @NonFinal
    @Value("${google.client-secret}")
    String clientSecret;

    @NonFinal
    @Value("${google.redirect-uri}")
    String redirectUri;

    @NonFinal
    @Value("${jwt.refresh-expiration}")
    Long refreshExpiration;

    @NonFinal
    @Value("${jwt.expiration}")
    Long accessExpiration;


    public boolean resetPassword(ResetPasswordRequest request){
        String email = request.getEmail();
        String key = "OTP:" +email;
        String otp = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        boolean isValid = Objects.equals(otp, request.getOtp());
        if(isValid){
            User user = userRepository.findByEmail(email);
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public String forgetPassword(String email){
        boolean emailVerified = userRepository.existsByEmail(email);
        if(!emailVerified){
            return "email is not exist";
        }else{
            String otp =  String.valueOf(new Random().nextInt(900000) + 100000);
            redisTemplate.opsForValue().set("OTP:" +email, otp, 5, TimeUnit.MINUTES);
            ForgetPasswordEvent event = ForgetPasswordEvent.builder()
                    .email(email)
                    .otp(otp)
                    .build();
            kafkaTemplate.send("forget-password", event);
            return "we just sent otp";
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse loginGoogle(GoogleLoginRequest request){
        GoogleExchangeTokenResponse response =  googleClient.getTokenFromGoogle(GoogleExchangeTokenRequest.builder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .code(request.getCode())
                        .grantType("authorization_code")
                        .redirectUri(redirectUri)
                .build());
        GoogleUserInfoResponse userInfo =
                googleUserInfoClient.getUserInfo("json", response.getAccessToken());
        if(!userRepository.existsByEmail(userInfo.getEmail())){
           User user = createUserFromGoogle(userInfo);
        }
        User user = userRepository.findByEmail(userInfo.getEmail());

        String accessToken = generateAccessToken(user, accessExpiration);
        String refreshToken = generateRefreshToken(user, refreshExpiration);


        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private User createUserFromGoogle(GoogleUserInfoResponse userInfo){
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        User user = userRepository.save(User.builder()
                .roles(roles)
                .email(userInfo.getEmail())
                .build());

        ProfileRequest profileRequest = ProfileRequest.builder()
                .fullName(userInfo.getName())
                .userId(user.getId())
                .email(userInfo.getEmail())
                .avatarUrl(userInfo.getPicture())
                .build();

        Object profile = profileClient.createProfile(profileRequest);

        // publish event to kafka
        kafkaTemplate.send("user-topic", CreationUserEvent.builder()
                .email(userInfo.getEmail())
                .build());
        return user;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        String accessToken = generateAccessToken(user, accessExpiration);
        String refreshToken = generateRefreshToken(user, refreshExpiration);

        UserResponse userResponse = userMapper.toUserResponse(user); // Gọn gàng hơn

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signToken = verifyToken(request.getToken());

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken());

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

//        var token = generateToken(user);
        String accessToken = generateAccessToken(user, accessExpiration);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    private String generateAccessToken(User user, Long accessExpiration){
        return generateToken(user, accessExpiration);
    }
    private String generateRefreshToken(User user, Long refreshExpiration){
        return generateToken(user, refreshExpiration);
    }

    private String generateToken(User user, Long expirationMs) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        String profileId = profileClient.getProfileByUserId(user.getId()).getResult().getId();

        Date expiryTime = new Date(System.currentTimeMillis() + expirationMs);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(profileId)
                .issuer("vidsonet.microservice.com")
                .issueTime(new Date())
                .expirationTime(expiryTime)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("userId", user.getId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }

    public void createPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        profileClient.updateHasPassword(ProfileRequest.builder()
                .email(request.getEmail()).build());
    }


    private record TokenInfo(String token, Date expiryDate) {}
}
