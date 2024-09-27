package com.example.coffeeOrderService.common.pageHandler;

import com.example.coffeeOrderService.dto.ProductDto;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Data
public class PageResponseDto<T> {

    private List<T> data;
    private Long nextCursor;  // 다음 페이지로 가져갈 커서

    // 생성자에서 데이터와 nextCursor를 처리
    public PageResponseDto(List<T> data) {
        this.data = data;
        this.nextCursor = extractNextCursor(data);
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
