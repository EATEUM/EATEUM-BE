package com.eateum.eateumbe.user.service.image;

import com.eateum.eateumbe.global.error.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 업로드 / 삭제 / 기본 이미지 처리
 */
@Slf4j
@Service
public class ProfileImageService {

    @Value("${file.upload.profile-dir}")
    private String profileDir;
    @Value("${file.upload.profile-url}")
    private String profileUrl;
    @Value("${file.upload.default-url}")
    private String defaultProfileImageUrl;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; //5MB

    /**
     * 프로필 이미지 업로드
     */
    public String upload(MultipartFile file) {
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
    public String resolve(String profileImage) {
        return (profileImage == null || profileImage.isBlank()) ? defaultProfileImageUrl : profileImage;
    }

    /**
     * 프로필 이미지가 없을때
     */
    public void delete(String imageUrl) {
        //이미지가 없거나 기본 이미지라면 삭제하지 않음
        if(imageUrl == null || imageUrl.equals(defaultProfileImageUrl)) {
            return;
        }

        //url 검증
        if (!imageUrl.startsWith(profileUrl)) {
            log.warn("Invalid profile image url: {}", imageUrl);
            return;
        }

        String fileName = imageUrl.substring(profileUrl.length());
        File file = new File(profileDir, fileName);

        if (file.exists() && !file.delete()) {
            log.warn("프로필 이미지 삭제 실패: {}", file.getAbsolutePath());
        }

    }

}
