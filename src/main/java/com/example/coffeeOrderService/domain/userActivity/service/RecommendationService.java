package com.example.coffeeOrderService.domain.userActivity.service;


import com.example.coffeeOrderService.domain.product.dto.PriceRangeDto;
import com.example.coffeeOrderService.domain.product.entity.Product;
import com.example.coffeeOrderService.domain.product.repository.ProductRepository;
import com.example.coffeeOrderService.domain.userActivity.entity.UserActivity;
import com.example.coffeeOrderService.domain.userActivity.repository.UserActivityRepository;
import com.example.coffeeOrderService.domain.userActivity.entity.Recommendation;
import com.example.coffeeOrderService.domain.userActivity.repository.RecommendationRepository;
import com.example.coffeeOrderService.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
            Product product = productService.getProductById(userActivity.getProductId());

            // 카테고리가 없는 상품은 추천 대상에서 제외
            if (product.getCategory() == null) {
                log.warn("Product {} has no category, skipping recommendation", product.getId());
                continue;
            }

            Long categoryId = product.getCategory().getId();
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
                    .map(similarProduct -> new Recommendation(userId, similarProduct.getId()))
                    .toList()
                    );
        }

        // 추천 데이터를 데이터베이스에 저장
        log.info("Generated {} recommendations for userId: {}", recommendations.size(), userId);
        if (!recommendations.isEmpty()) {  // 새로 만들 게 없으면 기존 걸 그냥 두고 싶다
            recommendationRepository.deleteByUserId(userId);   // ← 기존 추천 먼저 삭제, 중복 방지
            recommendationRepository.saveAll(recommendations);  // 새 추천 저장, 최신 목록 하나만 유지
        }
        return recommendations;
    }


}
