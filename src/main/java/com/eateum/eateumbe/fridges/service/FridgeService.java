package com.eateum.eateumbe.fridges.service;

import java.util.List;
import java.util.Map;

import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;



public interface FridgeService {



    /*
    나의 냉장고 재료 조회
    userID - 유저 아이디
    page - 페이지 번호
    size - 한 페이지에 보여줄 재료 개수
     */
    Map<String, Object> getMyFridgeItems(String userId, int page, int size);



    /*
    재료 검색
    */
    List<FridgeResponse> searchItems(String keyword);



}
