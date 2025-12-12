package com.eateum.eateumbe.fridges.domain;

import java.time.LocalDateTime;

import lombok.Data;


@Data
public class Fridge {


    private Long fridgeId;
    private String userId;
    private Long itemId;
    private LocalDateTime createdAt;
}
