package com.eateum.eateumbe.fridges.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eateum.eateumbe.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eateum.eateumbe.fridges.service.FridgeService;
import com.eateum.eateumbe.fridges.dto.request.FridgeRequest;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;
import static com.eateum.eateumbe.fridges.dto.response.FridgeResponse.*;


@RestController
@RequestMapping("/fridges")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;

    /*
    내 냉장고 조회(무한 스크롤 방식)
    ResponseEntity에 userId를 통해 재료들을 담는다.
    fridges?userId="userId"로 주소를 받아온다.
    required = false로 오류는 확인하지 않고 넘어간다.
     */
    @GetMapping
    public ApiResponse<FridgeListResponse> getFridge(
            @RequestParam(value = "userId", required = false) String userId,
            //기본 값 "1"로 설정해서 재료가 담긴 페이지 1은 기본으로 둔다.
            @RequestParam(value = "page", defaultValue = "1") int page,
            //1페이지 보여줄 개수 20개
            @RequestParam(value = "size", defaultValue = "20") int size) {

        //테스트용 ID 설정
        if(userId == null) {
            userId = "test-user-id";
        }

        FridgeListResponse result = fridgeService.getMyFridgeItems(userId, page, size);

        return ApiResponse.success(result);
    }

    /*
    재료 검색 API
     */
    @GetMapping("/search")
    public ApiResponse<List<FridgeResponse>> searchFridge(
            @RequestParam("keyword") String keyword
    ) {
        List<FridgeResponse> searchResult = fridgeService.searchItems(keyword);

        return ApiResponse.success(searchResult);
    }

    /*
    재료 추가 API(검색)
     */
    @PostMapping
    public ApiResponse<AddItem> addItem(
        @RequestBody FridgeRequest request
    ) {
        String userId = "test-user-id"; //테스트 아이디 임시 추가

        AddItem addedItem = fridgeService.addItem(userId, request);

        return new ApiResponse<>(true, "냉장고에 재료가 추가되었습니다.", addedItem);
    }

    /*
    재료 삭제(단건 삭제)
     */
    @DeleteMapping
    public ApiResponse<Void> deleteItem(
            @RequestParam("itemId") Long itemId
    ) {

        String userId = "test-user-id"; //테스트 아이디 임시 추가

        fridgeService.deleteItem(userId, itemId);

        return new ApiResponse<>(true, "재료 삭제 완료", null);
    }

}
