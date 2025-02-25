package com.example.capstone.githubtools.consumer;

import com.example.capstone.githubtools.dto.ScanMessageEvent;
import com.example.capstone.githubtools.model.ScanMessage;
import com.example.capstone.githubtools.service.ToolSchedulerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RepositoryConsumer {

    private final ToolSchedulerService toolSchedulerService;
    private final ObjectMapper objectMapper;

    public RepositoryConsumer(ToolSchedulerService toolSchedulerService, ObjectMapper objectMapper) {
        this.toolSchedulerService = toolSchedulerService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${kafka.topics.scan-pull-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(String message) {
        try {
            // 1) Convert the incoming JSON into a ScanMessageEvent
            ScanMessageEvent event = objectMapper.readValue(message, ScanMessageEvent.class);

            // 2) Extract the ScanMessage payload from the event
            ScanMessage scanMsg = event.getPayload();

            String eventId = event.getEventId();

            Long tenantId = scanMsg.getTenantId();
            String tool    = scanMsg.getTool();

            System.out.println("Consumer received => Event type: " + event.getType() +
                    ", tenantId=" + tenantId + ", tool=" + tool);

            // 3) Pass data to the service
            toolSchedulerService.processRepository(tenantId, tool, eventId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
