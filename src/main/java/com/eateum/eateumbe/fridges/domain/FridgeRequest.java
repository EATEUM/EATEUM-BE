package com.eateum.eateumbe.fridges.domain;

import lombok.Data;

@Data
public class FridgeRequest {

    //재료를 추가 할 때 재료 ID를 통해 확인
    private Long itemId;

}
