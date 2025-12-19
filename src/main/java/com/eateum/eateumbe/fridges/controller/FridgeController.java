package com.eateum.eateumbe.fridges.controller;

import java.util.List;

import com.eateum.eateumbe.global.common.BaseController;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.eateum.eateumbe.fridges.service.FridgeService;
import com.eateum.eateumbe.fridges.dto.request.FridgeRequest;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;
import com.eateum.eateumbe.global.common.ApiResponse;
import com.eateum.eateumbe.global.common.PageResponse;
import static com.eateum.eateumbe.fridges.dto.response.FridgeResponse.*;



@RestController
@RequestMapping("/fridges")
@RequiredArgsConstructor
public class FridgeController extends BaseController {

    private final FridgeService fridgeService;

    /*
    내 냉장고 조회(무한 스크롤 방식)
    fridges?userId="userId"로 주소를 받아온다.
     */
    @GetMapping
    public ApiResponse<PageResponse<FridgeResponse>> getFridge(
            @AuthenticationPrincipal String userId,
            //기본 값 "1"로 설정해서 재료가 담긴 페이지 1은 기본으로 둔다.
            @RequestParam(value = "page", defaultValue = "1") int page,
            //1페이지 보여줄 개수 20개
            @RequestParam(value = "size", defaultValue = "20") int size) {

        String safeUserId = resolveUserId(userId);

        PageResponse<FridgeResponse> result = fridgeService.getMyFridgeItems(safeUserId, page, size);

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
            @AuthenticationPrincipal String userId,
            @RequestBody FridgeRequest request
    ) {

        String safeUserId = resolveUserId(userId);

        AddItem addedItem = fridgeService.addItem(safeUserId, request);

        return new ApiResponse<>(true, "냉장고에 재료가 추가되었습니다.", addedItem);
    }

    /*
    재료 삭제(단건 삭제)
     */
    @DeleteMapping
    public ApiResponse<Void> deleteItem(
            @AuthenticationPrincipal String userId,
            @RequestParam("itemId") Long itemId
    ) {

        String safeUserId = resolveUserId(userId);

        fridgeService.deleteItem(safeUserId, itemId);

        return new ApiResponse<>(true, "재료 삭제 완료", null);
    }

    /*
    AI 이미지 인식 및 재료 추출 API(DB 조회용)
     */
    @PostMapping("/image-recognition")
    public ApiResponse<List<FridgeResponse>> recognizeItems(
            @RequestPart("file") MultipartFile file
    ){
        List<FridgeResponse> result = fridgeService.analyzeImage(file);

        if(result.isEmpty()) {
            return new ApiResponse<>(true, "이미지에서 일치하는 재료를 찾지 못했습니다.", result);
        }

        return new ApiResponse<>(true, "이미지 분석 및 재료 매칭 완료", result);
    }
    /*
    선택된 재료 일괄 추가 API
     */
    @PostMapping("/add-items")
    public ApiResponse<Void> addFridgeItems(
            @AuthenticationPrincipal String userId,
            @RequestBody List<Long> itemIds) {

        String safeUserId = resolveUserId(userId);

        fridgeService.addItems(safeUserId, itemIds);

        return new ApiResponse<>(true, "선택한 재료들이 냉장고에 추가되었습니다.", null);
    }

}
