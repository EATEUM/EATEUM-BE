package com.eateum.eateumbe.user.service;

import com.eateum.eateumbe.global.error.ApiException;
import com.eateum.eateumbe.global.jwt.JwtProperties;
import com.eateum.eateumbe.global.jwt.JwtProvider;
import com.eateum.eateumbe.global.redis.RefreshTokenService;
import com.eateum.eateumbe.user.domain.User;
import com.eateum.eateumbe.user.dto.request.LoginRequest;
import com.eateum.eateumbe.user.dto.request.PasswordChangeRequest;
import com.eateum.eateumbe.user.dto.request.SignupRequest;
import com.eateum.eateumbe.user.dto.request.UpdateInfoRequest;
import com.eateum.eateumbe.user.dto.response.LoginResponse;
import com.eateum.eateumbe.user.dto.response.UserInfoResponse;
import com.eateum.eateumbe.user.repository.UserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;

    @Value("${file.upload.profile-dir}")
    private String profileDir;
    @Value("${file.upload.profile-url}")
    private String profileUrl;
    @Value("${file.upload.default-url}")
    private String defaultProfileImageUrl;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; //5MB

    /**
     * 로그인 시 비밀번호를 검증하고 JWT토큰을 발행
     */
    @Override
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {

        //이메일로 사용자 조회
        User user = userMapper.findByEmail(request.getEmail());

        if(user == null){
            throw new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        //비밀번호 검증
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new ApiException(HttpStatus.UNAUTHORIZED, " 비밀번호가 일치하지 않습니다.");
        }

        //JWT payload 생성
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());

        //토큰 생성
        String accessToken = jwtProvider.createAccessToken(claims);
        String refreshToken = jwtProvider.createRefreshToken(claims);

        //쿠키 갱신
        saveRefreshToken(user.getUserId(), refreshToken, response);

        //AccessToken만 반환
        return new LoginResponse(accessToken);
    }

    /**
     * 액세스토큰 재발행
     */
    @Override
    public LoginResponse reissue(String refreshToken, HttpServletResponse response) {

        if(refreshToken == null){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다.");
        }

        //refreshToken 검증 + claims 추출
        Claims claims = jwtProvider.parseRefreshClaims(refreshToken);

        String userId = String.valueOf(claims.get("userId"));

        //Redis에 저장된 refreshToken 조회
        String savedRefreshToken = refreshTokenService.find(userId);

        log.info("refreshToken from cookie = {}", refreshToken);
        log.info("refreshToken from redis  = {}", savedRefreshToken);

        //Redis에 없거나 값이 다르면 실패
        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
        }

        Map<String, Object> newClaims = Map.of("userId", userId);

        //새 AccessToken 생성
        String newAccessToken = jwtProvider.createAccessToken(newClaims);

        //새 RefreshToken 생성
        String newRefreshToken = jwtProvider.createRefreshToken(newClaims);

        //쿠키 갱신
        saveRefreshToken(userId, newRefreshToken, response);

        //AccessToken만 반환
        return new LoginResponse(newAccessToken);
    }

    /**
     * RefreshToken을 Redis와 쿠키에 저장
     */
    private void saveRefreshToken(String userId, String refreshToken, HttpServletResponse response) {

        //Redis에 저장
        refreshTokenService.save(userId, refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) //localhost에서는 false
                .path("/user")
                .maxAge(Duration.ofSeconds(
                        jwtProperties.getRefreshToken().getExpireSeconds()
                ))
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

    }

    /**
     * 로그아웃
     * Redis와 Cookie에서 RefreshToken 삭제
     */
    @Override
    public void logout(String refreshToken, HttpServletResponse response) {

        //쿠키가 없으면 그냥 쿠키 삭제만 실행
        if (refreshToken != null) {
            try {
                Claims claims = jwtProvider.parseRefreshClaims(refreshToken);
                String userId = String.valueOf(claims.get("userId"));

                //Redis에서 RefreshToken 삭제
                refreshTokenService.delete(userId);
                log.info("[Logout] refresh token deleted. userId={}", userId);

            } catch (Exception e) {
                //만료 or 위조 토큰이어도 로그아웃은 성공 처리
                log.warn("[Logout] invalid refresh token");
            }
        }

        //RefreshTokenService 쿠키 제거
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // localhost에서는 false
                .path("/user")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());
    }

    /**
     * 프로필 조회
     */
    @Override
    public UserInfoResponse getUserInfo(String userId) {
        User user = getActiveUser(userId);

        return UserInfoResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .profileImage(resolveProfileImage(user.getProfileImage())) //프로필 이미지가 있는지?
                .build();
    }

    /**
     * 회원가입
     */
    @Override
    public void signup(SignupRequest signupRequest, MultipartFile profileImage) {

        //이메일 중복 체크
        if(userMapper.existsByEmail(signupRequest.getEmail()) > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        //프로필 이미지 업로드
        String imageUrl = null;
        if(profileImage != null && !profileImage.isEmpty()) {
            imageUrl = uploadProfileImage(profileImage);
        }

        //User 생성
        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .name(signupRequest.getName())
                .profileImage(imageUrl)
                .build();

        userMapper.insertUser(user);
    }

    /**
     * 프로필 이미지 업로드
     */
    private String uploadProfileImage(MultipartFile file) {
        try {

            //용량 체크
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "이미지 용량은 5MB 이하만 가능합니다.");
            }

            //MIME 타입 체크 (image/*)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드 가능합니다.");
            }

            //확장자 검증 (jpg, png, jpeg, webp)
            String originalName = file.getOriginalFilename(); //사용자가 올린 원래 파일명
            if(originalName == null || !originalName.contains(".")) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "파일명이 올바르지 않습니다.");
            }

            String safeOriginalName = originalName.replaceAll("[^a-zA-Z0-9._-]", "");

            String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();

            if (!List.of("jpg", "jpeg", "png", "webp").contains(extension)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 형식입니다.");
            }

            //폴더가 없으면 생성
            File dir = new File(profileDir);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지 저장 폴더 생성 실패");
            }

            //저장할 파일명
            String saveName = UUID.randomUUID() + "_" + safeOriginalName;
            //최종 저장 위치(파일 객체)
            File target = new File(profileDir, saveName);

            //실제 파일 저장
            file.transferTo(target);

            //DB에 저장할 값(접근용 경로 or URL)
            return profileUrl + saveName;

        } catch (ApiException e) {
            throw e; //우리가 던진 예외는 그대로
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지 업로드 실패");
        }
    }

    /**
     * 프로필 이미지 설정
     */
    private String resolveProfileImage(String profileImage) {
        return (profileImage == null || profileImage.isBlank()) ? defaultProfileImageUrl : profileImage;
    }

    /**
     * 프로필 수정
     */
    @Override
    public void updateInfo(String userId, UpdateInfoRequest updateInfoRequest, MultipartFile profileImage) {
        User user = getActiveUser(userId);

        //이름 수정
        if(updateInfoRequest.getName() != null && !updateInfoRequest.getName().isBlank()) {
            user.setName(updateInfoRequest.getName());
        }

        //프로필 이미지 수정
        if(profileImage != null && !profileImage.isEmpty()) {
            //기존 이미지 삭제 (기본 이미지 제외)
            deleteProfileImage(user.getProfileImage());

            //새 이미지 업로드
            String newImageUrl = uploadProfileImage(profileImage);
            user.setProfileImage(newImageUrl);
        }

        userMapper.updateUserInfo(user);
    }

    /**
     * 프로필 이미지 삭제
     */
    @Override
    public void deleteProfileImageOnly(String userId) {
        User user = getActiveUser(userId);

        deleteProfileImage(user.getProfileImage()); //기본 이미지라면 그냥 리턴
        user.setProfileImage(null); //아니라면 프로필 이미지 url을 null로

        userMapper.updateUserInfo(user); //수정
    }

    /**
     * 프로필 이미지가 없을때
     */
    private void deleteProfileImage(String imageUrl) {
        //이미지가 없거나 기본 이미지라면 삭제하지 않음
        if(imageUrl == null || imageUrl.equals(defaultProfileImageUrl)) {
            return;
        }

        String fileName = imageUrl.substring(profileUrl.length());
        File file = new File(profileDir, fileName);

        if (file.exists() && !file.delete()) {
            log.warn("프로필 이미지 삭제 실패: {}", file.getAbsolutePath());
        }

    }

    /**
     * 비밀번호 변경
     */
    @Override
    public void changePassword(String userId, PasswordChangeRequest passwordChangeRequest) {
        User user = userMapper.findByUserIdForPassword(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.");
        }

        //현재 비밀번호 일치 여부
        if(!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        //새 비밀번호
        if(!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "새 비밀번호가 일치하지 않습니다.");
        }

        //비밀번호 암호화 후 변경하기
        String encodedNewPassword = passwordEncoder.encode(passwordChangeRequest.getNewPassword());
        userMapper.updatePassword(userId, encodedNewPassword);

        //RefreshToken제거로 강제 로그아웃!
        refreshTokenService.delete(userId);
    }

    /**
     * 비밀번호 확인
     */
    @Override
    public void checkPassword(String password) {
        //현재 로그인 사용자 ID 정보 받기
        String userId = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        //조회
        User user = userMapper.findByUserIdForPassword(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 유효하지 않습니다.");
        }

        //비밀번호 검증
        if(!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        //성공시에는 아무것도 하지 않아도 됨!
    }

    /**
     * 회원탈퇴
     */
    @Override
    public void withdraw(String userId) {
        int withdrawCount = userMapper.withdraw(userId);

        if(withdrawCount == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "이미 탈퇴했거나 존재하지 않는 사용자입니다.");
        }

        //RefreshToken 삭제 (강제 로그아웃)
        refreshTokenService.delete(userId);
    }

    //인증된 활성 사용자 조회
    private User getActiveUser(String  userId) {
        User user = userMapper.findByUserId(userId);
        if(user == null){
            throw new ApiException(HttpStatus.UNAUTHORIZED, "인증 정보가 올바르지 않습니다.");
        }

        return user;
    }

}
