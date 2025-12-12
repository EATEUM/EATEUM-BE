package com.eateum.eateumbe.fridges.service;

import com.eateum.eateumbe.fridges.domain.FridgeResponse;
import com.eateum.eateumbe.fridges.repository.FridgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor //매퍼 연결 해주기
public class FridgeService {

    private final FridgeRepository fridgeRepository;

    /*
    나의 냉장고 재료 조회
    userID - 유저 아이디
    page - 페이지 번호
    size - 한 페이지에 보여줄 재료 개수
     */
    public Map<String, Object> getMyFridgeItems(String userId, int page, int size) {

        //offset을 설정하기 위함 페이지가 넘어가면 그 앞에 개수를 제외하고 순서대로 재료를 가지고 온다.
        int offset = (page - 1) * size;
        //Mapper에게 데이터 조회를 요청시킨다. (limit, offset을 전달)
        List<FridgeResponse> list = fridgeRepository.selectFridgeListByUserId(userId, size, offset);
        //전체 재료 개수 계산
        int totalItems = fridgeRepository.countTotalItems(userId);

        //재료 목록과 전체 개수를 묶어서 리턴 시킨다.
        Map<String, Object> resultmap = new HashMap<>();
        //재료 목록
        resultmap.put("list", list);
        //재료 총 개수
        resultmap.put("totalItems", totalItems);

        return resultmap;
    }

}
