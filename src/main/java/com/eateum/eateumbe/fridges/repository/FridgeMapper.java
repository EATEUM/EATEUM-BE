package com.eateum.eateumbe.fridges.repository;

import java.util.List;

import com.eateum.eateumbe.fridges.domain.Fridge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;
import static com.eateum.eateumbe.fridges.dto.response.FridgeResponse.*;


//MyBatis로 사용하면서 작업을 한다. JPA는 사용 안함
@Mapper
public interface FridgeMapper {

    //userId로 조회된 DB에서 재료[번호, 이름, 이미지]를 FridgeResponse에 담아 리스트로 변환
    List<Fridge> selectFridgeListByUserId(
            @Param("userId") String userId,
            //페이징 처리를 할 때 한번에 몇개를 가지고 오고 앞에서 몇개를 건너뛸 것인지 선정하기 위함.
            //size(재료를 한 번에 몇개를 가지고 올 것 인가)
            //offset(앞에서 몇개를 건너 뛰고 시작한 것인가(시작 위치 선정)
            @Param("size") int size,
            @Param("offset")  int offset);

    //나의 냉장고 재료가 총 몇개 있는지 확인한다.
    int countTotalItems(@Param("userId") String userId);

    //재료 검색 - keyword : 검색어 ("파") -> XML의 #{keyword}로 들어간다.
    List<Fridge> searchItem(@Param("keyword") String keyword);

    //재료 추가(검색)
    void addFridgeItem(@Param("userId") String userId, @Param("itemId") Long itemId);

    //단일 재료 상세 조회
    Fridge selectItemDetail(@Param("itemId") Long itemId);

    //재료 삭제 기능
    void deleteItem(@Param("userId") String userId, @Param("itemId") Long itemId);

    //회원 냉장고 재료 이름 리스트 조회 (AI 추천용)
    List<String> selectItemNamesByUserId(@Param("userId") String userId);

    //AI가 찾은 이름들로 재료 정보 조회
    List<Fridge> selectItemsByNames(@Param("names") List<String> names);

    //AI 이미지를 통해 재료 조회 후, 사용자 선택으로 재료들을 여러 개 추가 하기 위함
    void addFridgeItems(@Param("userId") String userId, @Param("itemIds") List<Long> itemIds);

}
