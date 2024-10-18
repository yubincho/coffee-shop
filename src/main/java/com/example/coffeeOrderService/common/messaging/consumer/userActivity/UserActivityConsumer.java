package com.example.coffeeOrderService.common.messaging.consumer.userActivity;

import com.example.coffeeOrderService.model.user.userActivity.UserActivity;
import com.example.coffeeOrderService.model.user.userActivity.UserActivityRepository;
import com.example.coffeeOrderService.service.userActivity.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

//@Profile("!local")  // 로컬 프로파일에서는 KafkaListener 를 사용하지 않음
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class UserActivityConsumer {

    private final UserActivityRepository userActivityRepository;
    private final RecommendationService recommendationService;


    @Value("${logging.file.path:/tmp}/dlq-log.txt")
    private String logFilePath;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000),
            autoCreateTopics = "false",
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${spring.kafka.topic.user-activity}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeUserActivity(UserActivity activity) {
        try {
            log.info("Consuming user activity: {}", activity);

            if (activity.getUserId() == null) {
                throw new IllegalArgumentException("User activity must have a user id");
            }

//        // 수신된 사용자 활동 데이터를 DB에 저장
            userActivityRepository.save(activity);

            // 추천 데이터 생성 (유저 ID 기반으로 추천 로직 호출)
            recommendationService.generateRecommendations(activity.getUserId());
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }


    @DltHandler
    public void handleDlt(UserActivity activity) {
        log.error("Failed to process user activity, sending to DLQ: {}", activity);
        // DLQ에 저장된 메시지를 처리 (예: 알림, 로그 저장 등)

        // 로그 파일에 메시지를 저장
        logToFile(activity);
    }

    /**
     * 사용자 활동 데이터를 로그 파일에 저장하는 메서드
     * @param activity 실패한 UserActivity 객체
     */
    private void logToFile(UserActivity activity) {
        try {
            // 로그 파일에 기록할 데이터 (ex: JSON 형식으로 변환하거나 CSV 형식)
//            String logData = "UserActivity failed: " + activity.toString();

            // UserActivity 객체를 JSON 형식으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String logData = objectMapper.writeValueAsString(activity);

            // 로그 파일 경로 지정
//            String logFilePath = "/path/to/dlq-log.txt";

            // 파일에 메시지 기록
            Files.write(Paths.get(logFilePath), logData.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            log.info("DLQ message logged to file: {}", logFilePath);

        } catch (IOException e) {
            log.error("Failed to log DLQ message to file", e);
        }
    }

}

