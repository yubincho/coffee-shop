package com.example.coffeeOrderService.common.messaging.producer;


import com.example.coffeeOrderService.model.user.userActivity.UserActivity;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class KafkaProducerConfig {

    // producer 인스턴스 생성 - factory
    // 기존의 KafkaTemplate<String, Long> 설정은 유지
    @Bean
    public ProducerFactory<String, Long> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class);;

        // 트랜잭션 관련 설정 추가
//        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-");
//        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        return new DefaultKafkaProducerFactory<>(config);
    }

    // 데이터 전송 - template
    @Bean
    public KafkaTemplate<String, Long> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }


    // KafkaTemplate<String, UserActivity> 설정
    @Bean
    public ProducerFactory<String, UserActivity> userActivityProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class); // UserActivity를 JSON으로 직렬화

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean("userActivityKafkaTemplate")
    public KafkaTemplate<String, UserActivity> userActivityKafkaTemplate() {
        return new KafkaTemplate<>(userActivityProducerFactory());
    }

    @Bean("defaultRetryTopicKafkaTemplate")
    public KafkaTemplate<String, UserActivity> retryableTopicKafkaTemplate(ProducerFactory<String, UserActivity> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
