package com.eateum.eateumbe.fridges.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eateum.eateumbe.fridges.dto.request.FridgeRequest;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse.AddItem;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;
import com.eateum.eateumbe.fridges.repository.FridgeRepository;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FridgeServiceImpl implements FridgeService {

    private final FridgeRepository fridgeRepository;

    //내 냉장고 재료 조회
    @Override
    public Map<String, Object> getMyFridgeItems(String userId, int page, int size) {
    //offset을 설정하기 위함 페이지가 넘어가면 그 앞에 개수를 제외하고 순서대로 재료를 가지고 온다.
    int offset = (page - 1) * size;
    //Mapper에게 데이터 조회를 요청시킨다. (limit, offset을 전달)
    List<FridgeResponse> list = fridgeRepository.selectFridgeListByUserId(userId, size, offset);
    //전체 재료 개수 계산
    int totalItems = fridgeRepository.countTotalItems(userId);

    //재료 목록과 전체 개수를 묶어서 리턴 시킨다.
    Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", list);//재료 목록
        resultMap.put("totalItems", totalItems);//재료 총 개수

        return resultMap;
    }

    //재료 검색
    @Override
    public List<FridgeResponse> searchItems(String keyword) {
        return fridgeRepository.searchItem(keyword);
    }

    //재료 추가(검색)
    @Override
    @Transactional
    public AddItem addItem(String userId, FridgeRequest request) {
        fridgeRepository.addFridgeItem(userId, request.getItemId());

        AddItem addedItem = fridgeRepository.selectItemDetail(request.getItemId());

        return addedItem;
    }
}
