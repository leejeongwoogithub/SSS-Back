package com.example.demo.domain.order.controller.request;

import com.example.demo.domain.order.entity.items.ItemCategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CartItemDeleteRequest {

    final private Long itemId;

    @JsonProperty("itemCategory")
    final private ItemCategoryType itemCategoryType;

}