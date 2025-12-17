package com.eateum.eateumbe.fridges.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.eateum.eateumbe.fridges.dto.request.FridgeRequest;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse.AddItem;
import com.eateum.eateumbe.global.common.PageResponse;


public interface FridgeService {

    /*
    나의 냉장고 재료 조회
    userID - 유저 아이디
    page - 페이지 번호
    size - 한 페이지에 보여줄 재료 개수
     */
    PageResponse<FridgeResponse> getMyFridgeItems(String userId, int page, int size);

    /*
    재료 검색
    */
    List<FridgeResponse> searchItems(String keyword);


    /*
    재료 추가 기능(검색)
     */
    AddItem addItem(String userId, FridgeRequest request);


    /*
    재료 삭제
     */
    void deleteItem(String userId, Long itemId);

    /*
    이미지 인식 후 DB에 있는 재료 목록 반환
     */
    List<FridgeResponse> analyzeImage(MultipartFile image);
    /*
    선택된 여러 재료를 한 번에 추가
     */
    void addItems(String userId, List<Long> itemIds);
}
