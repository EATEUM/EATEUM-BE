package com.eateum.eateumbe.fridges.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FridgeResponse {

    //단일 재료 정보
    // 재료 번호, 재료 이름, 재료 사진을 보여준다.
    private Long itemId;
    private String itemName;
    private String itemImg;

    //재료 추가
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddItem{
        private Long itemId;
        private String itemName;
        private String itemImg;
    }

}
