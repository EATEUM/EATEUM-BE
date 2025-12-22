package com.eateum.eateumbe.fridges.service;

import java.util.List;

import com.eateum.eateumbe.fridges.domain.Fridge;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eateum.eateumbe.fridges.dto.request.FridgeRequest;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;
import com.eateum.eateumbe.fridges.dto.response.FridgeResponse.AddItem;
import com.eateum.eateumbe.fridges.repository.FridgeMapper;
import com.eateum.eateumbe.global.common.PageResponse;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
public class FridgeServiceImpl implements FridgeService {

    private final FridgeMapper fridgeMapper;
    private final GeminiService geminiService;

    @Override
    public List<String> getGuestItemNames(){
        return List.of("돼지고기", "스팸", "달걀", "김치", "라면", "양파", "파");
    }

    //내 냉장고 재료 조회
    @Override
    public PageResponse<FridgeResponse> getMyFridgeItems(String userId, int page, int size) {

        //비회원인 경우: DB 조회 없이 고정된 7가지 재료 반환
        if("guest".equalsIgnoreCase(userId)){
            List<String> guestItemNames = getGuestItemNames();

            List<Fridge> guestEntities = fridgeMapper.selectItemsByNames(guestItemNames);
            List<FridgeResponse> guestList = guestEntities.stream()
                    .map(FridgeResponse::from)
                    .toList();

            return PageResponse.of(guestList, guestList.size(), page, size);
        }

        //회원인 경우
        //offset을 설정하기 위함 페이지가 넘어가면 그 앞에 개수를 제외하고 순서대로 재료를 가지고 온다. (무한 스크롤 진행이라도 몇 개인지 기준이 필요함)
        int offset = (page - 1) * size;

        List<Fridge> entities = fridgeMapper.selectFridgeListByUserId(userId, size, offset);

        //Mapper에게 데이터 조회를 요청시킨다. (limit, offset을 전달)
        List<FridgeResponse> list = entities.stream()
                .map(FridgeResponse::from)
                .toList();

        //전체 재료 개수 계산
        int totalItems = fridgeMapper.countTotalItems(userId);
        return PageResponse.of(list, totalItems, page, size);
    }

    //재료 검색
    @Override
    public List<FridgeResponse> searchItems(String keyword) {
        return fridgeMapper.searchItem(keyword).stream()
                .map(FridgeResponse::from)
                .toList();
    }

    //재료 추가(검색)
    @Override
    @Transactional
    public AddItem addItem(String userId, FridgeRequest request) {
        if ("guest".equalsIgnoreCase(userId)) {
            throw new IllegalArgumentException("재료를 저장하려면 로그인이 필요합니다.");
        }

        // 1. 현재 사용자의 냉장고 재료 이름 리스트를 가져옴
        List<String> currentItems = fridgeMapper.selectItemNamesByUserId(userId);

        // 2. 추가하려는 재료의 정보를 가져옴
        Fridge targetItem = fridgeMapper.selectItemDetail(request.getItemId());

        // 3. 중복 체크
        if (targetItem != null && currentItems.contains(targetItem.getItemName())) {
            throw new IllegalArgumentException("이미 냉장고에 존재하는 재료입니다.");
        }

        try {
            fridgeMapper.addFridgeItem(userId, request.getItemId());
        } catch (Exception e) {
            throw new RuntimeException("재료 추가 중 서버 오류가 발생했습니다.");
        }

        return AddItem.from(targetItem);
    }
    //재료 삭제
    @Override
    @Transactional
    public void deleteItem(String userId, Long itemId) {
        //비회원은 재료 추가 불가
        if("guest".equalsIgnoreCase(userId)){
            throw new IllegalArgumentException("재료를 삭제하려면 로그인이 필요합니다.");
        }

        fridgeMapper.deleteItem(userId, itemId);
    }

    //이미지 재료들을 DB와 맞는지 조회
    @Override
    public List<FridgeResponse> analyzeImage(MultipartFile file) {
        List<String> detectedNames = geminiService.uploadImg(file);

        if(detectedNames == null || detectedNames.isEmpty()){
            return List.of();
        }

        List<Fridge> entities = fridgeMapper.selectItemsByNames(detectedNames);

        return entities.stream().map(FridgeResponse::from).toList();
    }
    //이미지 인식 후 재료 추가
    @Override
    @Transactional
    public void addItems(String userId, List<Long> itemIds) {
        // 1. 비회원 체크
        if ("guest".equalsIgnoreCase(userId)) {
            throw new IllegalArgumentException("재료를 저장하려면 로그인이 필요합니다.");
        }

        if (itemIds == null || itemIds.isEmpty()) return;

        List<String> currentItemNames = fridgeMapper.selectItemNamesByUserId(userId);

        List<Long> newItemsToSave = itemIds.stream()
                .filter(itemId -> {
                    Fridge item = fridgeMapper.selectItemDetail(itemId);
                    return item != null && !currentItemNames.contains(item.getItemName());
                })
                .toList();

        if (!newItemsToSave.isEmpty()) {
            fridgeMapper.addFridgeItems(userId, newItemsToSave);
        }
    }
}
