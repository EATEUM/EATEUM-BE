package com.eateum.eateumbe.fridges.domain;

import lombok.Data;

@Data
public class FridgeResponse {

    //재료 번호, 재료 이름, 재료 사진을 보여준다.
    private Long itemId;
    private String itemName;
    private String itemImg;

}
