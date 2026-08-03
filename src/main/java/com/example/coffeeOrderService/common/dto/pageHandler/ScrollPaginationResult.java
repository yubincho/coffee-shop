package com.example.coffeeOrderService.common.dto.pageHandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class ScrollPaginationResult<T> {
    private List<T> content;
    private boolean hasNext;
}
