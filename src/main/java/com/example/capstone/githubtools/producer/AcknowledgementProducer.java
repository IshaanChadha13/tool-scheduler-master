package com.example.capstone.githubtools.producer;

import com.example.capstone.githubtools.dto.ScanAcknowledgement;
import com.example.capstone.githubtools.model.AcknowledgementEvent;
import com.example.capstone.githubtools.model.AcknowledgementStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AcknowledgementProducer {

    @Value("${kafka.topics.job-acknowledgement-topic}")
    private String jobAckTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AcknowledgementProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends a ScanAcknowledgement to the job-acknowledgement-topic.
     *
     * @param jobId   the ID (eventId) of the job as received from JFC
     * @param success true if the tool-scheduler completed its job successfully, false otherwise
     */
    public void sendScanAcknowledgement(String jobId, boolean success) {
        try {
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(jobId);
            ackEvent.setStatus(success ? AcknowledgementStatus.SUCCESS : AcknowledgementStatus.FAILURE);
            ScanAcknowledgement ack = new ScanAcknowledgement(null, ackEvent);
            String json = objectMapper.writeValueAsString(ack);
            kafkaTemplate.send(jobAckTopic, json);
            System.out.println("Tool-Scheduler sent ScanAcknowledgement: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
