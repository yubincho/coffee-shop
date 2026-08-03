package com.example.coffeeOrderService.common.dto.pageHandler.dto;

import com.example.coffeeOrderService.domain.product.dto.ProductDto;
import lombok.Data;

import java.util.List;


@Data
public class PageResponseDto<T> {

    private List<T> data;
    private Long nextCursor;  // 다음 요청용 커서(= 이번 페이지 마지막 항목의 id)
    private boolean hasNext;

    // 생성자에서 데이터와 nextCursor를 처리
    public PageResponseDto(List<T> data, boolean hasNext) {
        this.data = data;
        this.nextCursor = extractNextCursor(data);
        this.hasNext = hasNext;
    }

    // 마지막 항목의 ID를 추출하여 nextCursor 설정
    private Long extractNextCursor(List<T> data) {
        if (!data.isEmpty() && data.get(0) instanceof ProductDto) {
            // 마지막 ProductDto의 id를 nextCursor로 설정
            return ((ProductDto) data.get(data.size() - 1)).getId();
        }
        return null;
    }

}
