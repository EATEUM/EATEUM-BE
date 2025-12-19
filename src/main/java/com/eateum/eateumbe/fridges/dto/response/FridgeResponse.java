package com.eateum.eateumbe.fridges.dto.response;

import com.eateum.eateumbe.fridges.domain.Fridge;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FridgeResponse {

    //단일 재료 정보
    // 재료 번호, 재료 이름, 재료 사진을 보여준다.
    private Long itemId;
    private String itemName;
    private String itemImg;

    public static FridgeResponse from(Fridge fridge) {
        return FridgeResponse.builder()
                .itemId(fridge.getItemId())
                .itemName(fridge.getItemName())
                .itemImg(fridge.getItemImg())
                .build();

    }

    //재료 추가
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddItem{
        private Long itemId;
        private String itemName;
        private String itemImg;

        public static AddItem from(Fridge fridge) {
            return AddItem.builder()
                    .itemId(fridge.getItemId())
                    .itemName(fridge.getItemName())
                    .itemImg(fridge.getItemImg())
                    .build();
        }
    }

}
