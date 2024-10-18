package com.example.coffeeOrderService.service.userActivity;


import com.example.coffeeOrderService.dto.PriceRangeDto;
import com.example.coffeeOrderService.model.product.Product;
import com.example.coffeeOrderService.model.product.ProductRepository;
import com.example.coffeeOrderService.model.user.recommandation.Recommendation;
import com.example.coffeeOrderService.model.user.recommandation.RecommendationRepository;
import com.example.coffeeOrderService.model.user.userActivity.UserActivity;
import com.example.coffeeOrderService.model.user.userActivity.UserActivityRepository;
import com.example.coffeeOrderService.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 추천하기 로직
@Slf4j
@RequiredArgsConstructor
@Service
public class RecommendationService {

    private final UserActivityRepository userActivityRepository;
    private final RecommendationRepository recommendationRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;


    // 추천 데이터 생성
    public List<Recommendation> generateRecommendations(long userId) {

        // 사용자의 장바구니 기록을 조회
        List<UserActivity> userActivities = userActivityRepository.findByUserIdAndAction(userId, "ADD_TO_CART");

        if (userActivities.isEmpty()) {
            log.error("No user activities found for userId: {}", userId);
            return new ArrayList<>();
        }

        // 각 장바구니 아이템에 대해 유사한 제품 찾기
        List<Recommendation> recommendations = new ArrayList<>();
        for (UserActivity userActivity : userActivities) {
            Long categoryId = productService.getProductById(userActivity.getProductId()).getCategory().getId();
            Long productId = userActivity.getProductId();

            // min price 와 max price 찾기
            PriceRangeDto priceRangeDto = productRepository.findMinPriceAndMaxPrice(productId);

            if (priceRangeDto == null) {
                log.error("No price range found for productId: {}", productId);
                continue; // null을 발견하면 다음으로 건너뜀
            }

            // 같은 카테고리에 속하는 다른 제품 찾기
            List<Product> similarProducts = productRepository.findSimilarProducts(categoryId, priceRangeDto.getMinPrice(),
                    priceRangeDto.getMaxPrice(), productId);

            if (similarProducts == null || similarProducts.isEmpty()) {
                log.warn("No similar products found for categoryId: {}, productId: {}", categoryId, productId);
                continue;
            }

            // 유사 제품을 추천 목록에 추가 (5개 제한)
            recommendations.addAll(similarProducts.stream()
                    .limit(5)
                    .map(product -> new Recommendation(userId, product.getId()))
                    .toList()
                    );
        }

        // 추천 데이터를 데이터베이스에 저장
        recommendationRepository.saveAll(recommendations);

        return recommendations;
    }


}
