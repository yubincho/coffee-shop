package com.example.coffeeOrderService.common.pageHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@Builder
@AllArgsConstructor
@Data
public class PageRequestDto {

//    private int page;
    private Long cursor;
    private String keyword;
    private Integer size;  // null 문제로 Integer로 변경

    public PageRequestDto() {
        this.cursor = null;
        this.keyword = null;
        this.size = 10;
    }

//    public Pageable getPageable(Sort sort) {
//        return PageRequest.of(page - 1, size, sort);
//    }
    public Pageable getPageable(Sort sort) {
        return PageRequest.of(0, size, sort);  // 커서 페이징에서는 오프셋이 의미 없으므로 0
    }

}
