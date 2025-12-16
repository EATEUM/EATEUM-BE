package com.eateum.eateumbe.fridges.domain;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fridge {

    private String userId;
    private Long itemId;
    private LocalDateTime createdAt;

    private String itemName;
    private String itemImg;

}
