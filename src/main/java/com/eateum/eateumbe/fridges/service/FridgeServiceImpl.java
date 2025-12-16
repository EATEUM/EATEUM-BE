package com.eateum.eateumbe.fridges.service;

import java.util.List;

import com.eateum.eateumbe.global.common.PageResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eateum.eateumbe.fridges.dto.request.FridgeRequest;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse.AddItem;
import com.eateum.eateumbe.fridges.repository.FridgeRepository;


@Service
@RequiredArgsConstructor
public class FridgeServiceImpl implements FridgeService {

    private final FridgeRepository fridgeRepository;

    //내 냉장고 재료 조회
    @Override
    public PageResponse<FridgeResponse> getMyFridgeItems(String userId, int page, int size) {
        //offset을 설정하기 위함 페이지가 넘어가면 그 앞에 개수를 제외하고 순서대로 재료를 가지고 온다. (무한 스크롤 진행이라도 몇 개인지 기준이 필요함)
        int offset = (page - 1) * size;
        //Mapper에게 데이터 조회를 요청시킨다. (limit, offset을 전달)
        List<FridgeResponse> list = fridgeRepository.selectFridgeListByUserId(userId, size, offset);
        //전체 재료 개수 계산
        int totalItems = fridgeRepository.countTotalItems(userId);

        return PageResponse.of(list, totalItems, page, size);
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

        return fridgeRepository.selectItemDetail(request.getItemId());
    }

    //재료 삭제
    @Override
    @Transactional
    public void deleteItem(String userId, Long itemId) {
        fridgeRepository.deleteItem(userId, itemId);
    }
}
