package com.dan1yal.orderservice.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final Environment environment;

    public Map<String, Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, environment.getProperty("spring.kafka.bootstrap-servers"));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, environment.getProperty("spring.kafka.producer.acks"));
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, environment.getProperty("spring.kafka.producer.delivery-timeout-ms"));
        config.put(ProducerConfig.LINGER_MS_CONFIG, environment.getProperty("spring.kafka.producer.linger-ms"));
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, environment.getProperty("spring.kafka.producer.request-timeout-ms"));
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, environment.getProperty("spring.kafka.producer.enable-idempotence"));
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, environment.getProperty("spring.kafka.producer.max-in-flight-requests-per-connection"));
        config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, environment.getProperty("spring.kafka.producer.transactional-id"));

        return config;
    }

    @Bean
    ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
