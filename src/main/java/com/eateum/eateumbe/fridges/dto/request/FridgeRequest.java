package com.eateum.eateumbe.fridges.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FridgeRequest {

    //재료 조회, 추가, 삭제 할 때 재료 ID를 통해 확인
    private Long itemId;

}
