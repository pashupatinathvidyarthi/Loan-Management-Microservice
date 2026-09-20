package com.ltf.loanmanagement.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the topics this service owns. 3 partitions lets multiple consumer
 * instances in the same group share the load and process different customers'
 * events in parallel, while events for the SAME loanApplicationId still land on
 * the same partition (see the producer's keying) so per-loan ordering is preserved.
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic loanApplicationEventsTopic() {
        return TopicBuilder.name("loan-application-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic creditCheckResultsTopic() {
        return TopicBuilder.name("credit-check-results")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
