package com.example.capstone.githubtools.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Updated to use the new scan-pull topic for communication between JFC and tool-scheduler
    @Value("${kafka.topics.scan-pull-topic}")
    private String scanPullTopic;

    @Value("${kafka.topics.scan-parse-topic}")
    private String scanParseTopic;

    // Optionally, if the tool-scheduler is also responsible for producing acknowledgements,
    // you can define the job-acknowledgement topic as well.
    @Value("${kafka.topics.job-acknowledgement-topic}")
    private String jobAckTopic;

    @Value("${kafka.topics.partition-count}")
    private int partitionCount;

    @Value("${kafka.topics.replication-factor}")
    private short replicationFactor;

    @Bean
    public NewTopic scanPullTopic() {
        return TopicBuilder.name(scanPullTopic)
                .partitions(partitionCount)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic scanParseTopic() {
        return TopicBuilder.name(scanParseTopic)
                .partitions(partitionCount)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic jobAckTopic() {
        return TopicBuilder.name(jobAckTopic)
                .partitions(partitionCount)
                .replicas(replicationFactor)
                .build();
    }
}
