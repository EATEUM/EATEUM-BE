package com.eateum.eateumbe.fridges.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class FridgeResponse {

    //재료 번호, 재료 이름, 재료 사진을 보여준다.
    private Long itemId;
    private String itemName;
    private String itemImg;

    //재료 추가
    @Data
    @AllArgsConstructor
    public static class AddItem{
        private Long itemId;
        private String itemName;
        private String itemImg;
    }

}
