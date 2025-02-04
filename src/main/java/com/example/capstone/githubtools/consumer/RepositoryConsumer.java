package com.example.capstone.githubtools.consumer;

import com.example.capstone.githubtools.dto.RepoMessage;
import com.example.capstone.githubtools.service.ToolSchedulerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryConsumer {

    private final ToolSchedulerService toolSchedulerService;
    private final ObjectMapper objectMapper;

    public RepositoryConsumer(ToolSchedulerService toolSchedulerService, ObjectMapper objectMapper) {
        this.toolSchedulerService = toolSchedulerService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${kafka.topics.repo-schedule-topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(String message) {
        try {
            ScanMessage scanMsg = objectMapper.readValue(message, ScanMessage.class);

            String owner = scanMsg.getOwner();
            String repo  = scanMsg.getRepo();
            List<String> tools = scanMsg.getTools(); // the user-chosen tools

            System.out.println("Consumer received: " + owner + "/" + repo +
                    " with tools: " + tools);

            // calls the service to fetch GitHub alerts only for those tools
            toolSchedulerService.processRepository(owner, repo, tools);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ScanMessage {
        private String owner;
        private String repo;
        private List<String> tools;

        public String getOwner() { return owner; }
        public String getRepo() { return repo; }
        public List<String> getTools() { return tools; }
    }
}
