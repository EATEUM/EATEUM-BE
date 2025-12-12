package com.eateum.eateumbe.fridges.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eateum.eateumbe.fridges.dto.response.FridgeResponse;

//MyBatis로 사용하면서 작업을 한다. JPA는 사용 안함
@Mapper
public interface FridgeRepository {

    //userId로 조회된 DB에서 재료[번호, 이름, 이미지]를 FridgeResponse에 담아 리스트로 변환
    List<FridgeResponse> selectFridgeListByUserId(
            @Param("userId") String userId,
            //페이징 처리를 할 때 한번에 몇개를 가지고 오고 앞에서 몇개를 건너뛸 것인지 선정하기 위함.
            //limit(재료를 한 번에 몇개를 가지고 올 것 인가)
            //offset(앞에서 몇개를 건너 뛰고 시작한 것인가(시작 위치 선정)
            @Param("limit") int limit,
            @Param("offset")  int offset);

    //나의 냉장고 재료가 총 몇개 있는지 확인한다.
    int countTotalItems(@Param("userId") String userId);

    //재료 검색 - keyword : 검색어 ("파") -> XML의 #{keyword}로 들어간다.
    List<FridgeResponse> searchItem(@Param("keyword") String keyword);



}
