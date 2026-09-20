package com.ltf.loanmanagement.config;

import com.ltf.loanmanagement.kafka.CreditCheckResultEvent;
import com.ltf.loanmanagement.kafka.LoanApplicationEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot's autoconfigured KafkaTemplate is typed KafkaTemplate<Object, Object>,
 * which does NOT satisfy generics-aware autowiring for injection points like
 * KafkaTemplate<String, LoanApplicationEvent> (Spring's generic bean matching is
 * invariant, not covariant). Declaring strongly-typed producer factories/templates
 * explicitly avoids a "no qualifying bean" failure at startup.
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return props;
    }

    @Bean
    public ProducerFactory<String, LoanApplicationEvent> loanApplicationEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(baseProducerProps());
    }

    @Bean
    public KafkaTemplate<String, LoanApplicationEvent> kafkaTemplate(
            ProducerFactory<String, LoanApplicationEvent> loanApplicationEventProducerFactory) {
        return new KafkaTemplate<>(loanApplicationEventProducerFactory);
    }

    @Bean
    public ProducerFactory<String, CreditCheckResultEvent> creditCheckResultProducerFactory() {
        return new DefaultKafkaProducerFactory<>(baseProducerProps());
    }

    @Bean
    public KafkaTemplate<String, CreditCheckResultEvent> resultKafkaTemplate(
            ProducerFactory<String, CreditCheckResultEvent> creditCheckResultProducerFactory) {
        return new KafkaTemplate<>(creditCheckResultProducerFactory);
    }
}
