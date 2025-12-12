package com.eateum.eateumbe.fridges.controller;

import com.eateum.eateumbe.fridges.domain.FridgeResponse;
import com.eateum.eateumbe.fridges.service.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fridges")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;

    /*
    ResponseEntity에 userId를 통해 재료들을 담는다.
    fridges?userId="userId"로 주소를 받아온다.
    required = false로 오류는 확인하지 않고 넘어간다.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFridge(
            @RequestParam(value = "userId", required = false) String userId,
            //기본 값 "1"로 설정해서 재료가 담긴 페이지 1은 기본으로 둔다.
            @RequestParam(value = "page", defaultValue = "1") int page,
            //1페이지 보여줄 개수 10개
            @RequestParam(value = "size", defaultValue = "10") int size) {

        //테스트용 ID 설정
//        if(userId == null) {
//            userId = "test-user-id";
//        }

        //Map을 사용해 userId를 통해서 재료 목록과 재료 전체 개수를 받아온다.
        Map<String, Object> serviceResult = fridgeService.getMyFridgeItems(userId, page, size);

        //위에서 Service를 통해서 받는 데이터를 "list"(재료 목록)을 확인하기 위해 fridgeItems로 받은 List로 꺼내기
        List<FridgeResponse> fridgeItems = (List<FridgeResponse>) serviceResult.get("list");
        //Service에 있는 총 개수 totalItems로 꺼내오기
        int totalItems = (int) serviceResult.get("totalItems");
        //페이지 설정하기 위한 계산 -> 총 페이지 수 = 올림(전체 개수 / 한 페이지 개수)
        //예시) 재료가 47개라면 -> 47 / 10 = 4.7이 나올 것이기에 올림 처리하여 5페이지 처리
        int totalPages = (int) Math.ceil((double) totalItems / size);

        //다음 페이지가 있는가? (현재 페이지 < 총 페이지)
        boolean hasNextPage = page < totalPages;
        //이전 페이지가 있는가? (현재 페이지 > 1)
        boolean hasPreviousPage = page > 1;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        Map<String, Object> data = new HashMap<>();
        data.put("fridgeItems", fridgeItems);

        //pagination 정보 만들기
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("currentPage", page); //현재 몇 페이지인지
        pagination.put("totalPages", totalPages); // 총 몇 페이지인지
        pagination.put("totalItems", totalItems); // 총 재료가 몇개인지
        pagination.put("perPage", size); //한 페이지에 몇개의 재료가 있는지
        pagination.put("hasNextPage", hasNextPage);//다음 페이지가 존재하는지 확인
        pagination.put("hasPreviousPage", hasPreviousPage);//이전 페이지가 있는지 확인

        data.put("pagination", pagination);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

}
