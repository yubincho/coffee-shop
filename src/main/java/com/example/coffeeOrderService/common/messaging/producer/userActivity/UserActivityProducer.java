package com.example.coffeeOrderService.common.messaging.producer.userActivity;

import com.example.coffeeOrderService.model.user.userActivity.UserActivity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class UserActivityProducer {

    private final KafkaTemplate<String, UserActivity> kafkaTemplate;


    @Value("${spring.kafka.topic.user-activity}")
    private String topic;


    public UserActivityProducer(@Qualifier("userActivityKafkaTemplate") KafkaTemplate<String, UserActivity> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 사용자 활동을 Kafka 토픽으로 전송
    public void sendUserActivity(UserActivity userActivity) {
        kafkaTemplate.send(topic, userActivity);
    }
}
