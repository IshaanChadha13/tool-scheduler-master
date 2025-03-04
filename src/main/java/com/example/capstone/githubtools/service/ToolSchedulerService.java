package com.example.capstone.githubtools.service;

import com.example.capstone.githubtools.dto.ParserMessageEvent;
import com.example.capstone.githubtools.githubclient.GithubApiClient;
import com.example.capstone.githubtools.model.ParserMessage;
import com.example.capstone.githubtools.model.TenantEntity;
import com.example.capstone.githubtools.producer.AcknowledgementProducer;
import com.example.capstone.githubtools.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class ToolSchedulerService {

    private final TenantRepository tenantRepository;
    private final GithubApiClient githubApiClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AcknowledgementProducer acknowledgementProducer; // New field

    @Value("${kafka.topics.jfc-jobs}")
    private String jfcJobsTopic;

    @Value("${myapp.local-storage}")
    private String localStoragePath;

    public ToolSchedulerService(
            TenantRepository tenantRepository,
            GithubApiClient githubApiClient,
            KafkaTemplate<String, String> kafkaTemplate,
            AcknowledgementProducer acknowledgementProducer
    ) {
        this.tenantRepository = tenantRepository;
        this.githubApiClient = githubApiClient;
        this.kafkaTemplate = kafkaTemplate;
        this.acknowledgementProducer = acknowledgementProducer;

    }

    /**
     * Process repository for the given tenant & tool.
     */
    public void processRepository(Long tenantId, String tool, String eventId) {
        // 1) Find TenantEntity from DB
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("No tenant found for id=" + tenantId));

        String owner = tenant.getOwner();
        String repo = tenant.getRepo();
        String pat  = tenant.getPersonalAccessToken();

        if (owner == null || repo == null || pat == null) {
            System.out.println("Tenant " + tenantId + " is missing owner/repo/PAT");
            return;
        }

        // 2) Create base directory => e.g. "tenant-2_ishaan-juice-shop"
        String baseFolder = localStoragePath + File.separator
                + "tenant-" + tenantId + "_" + owner + "-" + repo;
        File baseDir = new File(baseFolder);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        // 3) Fetch & store alerts for the selected tool
        if (Objects.equals(tool, "CODE_SCANNING")) {
            var codeAlerts = githubApiClient.fetchCodeScanningAlerts(owner, repo, pat);
            writeAndSendToParser(tenantId, owner, repo, "CODE_SCANNING", codeAlerts, eventId);
        } else if (Objects.equals(tool, "DEPENDABOT")) {
            var dependabotAlerts = githubApiClient.fetchDependabotAlerts(owner, repo, pat);
            writeAndSendToParser(tenantId, owner, repo, "DEPENDABOT", dependabotAlerts, eventId);
        } else if (Objects.equals(tool, "SECRET_SCANNING")) {
            var secretAlerts = githubApiClient.fetchSecretScanningAlerts(owner, repo, pat);
            writeAndSendToParser(tenantId, owner, repo, "SECRET_SCANNING", secretAlerts, eventId);
        }
    }

    /**
     * Write alerts to a JSON file, then publish a ParserMessageEvent with {tenantId, filePath} to parser-topic.
     */
    private void writeAndSendToParser(Long tenantId, String owner, String repo,
                                      String toolType, List<Map<String,Object>> alerts,String eventId) {

        // e.g. "/tmp/alerts/tenant-2_ishaan-juice-shop/CODE_SCANNING"
        String subFolderPath = localStoragePath
                + File.separator + "tenant-" + tenantId + "_" + owner + "-" + repo
                + File.separator + toolType;

        File subFolder = new File(subFolderPath);
        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }

        // e.g. "tenant-2_ishaan-juice-shop-CODE_SCANNING-<timestamp>.json"
        String fileName = "tenant-" + tenantId + "_" + owner + "-" + repo
                + "-" + toolType + "-" + System.currentTimeMillis() + ".json";

        File outFile = new File(subFolder, fileName);
        writeToJsonFile(alerts, outFile);

        // Create ParserMessage
        ParserMessage parserMessage = new ParserMessage(tenantId, outFile.getAbsolutePath(), toolType);

        // Wrap it into ParserMessageEvent
        String parseEventId = UUID.randomUUID().toString();  // a new unique ID

        ParserMessageEvent parserMessageEvent = new ParserMessageEvent(parserMessage, parseEventId, "jfc-parser-topic");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        try {
            // Convert the event to JSON
            String json = objectMapper.writeValueAsString(parserMessageEvent);

            // Publish to Kafka
            kafkaTemplate.send(jfcJobsTopic, json);

            System.out.printf(
                    "Saved %s alerts to %s. Produced ParserMessageEvent => %s%n",
                    toolType, outFile.getAbsolutePath(), json
            );

            acknowledgementProducer.sendScanAcknowledgement(eventId, true);

        } catch (IOException e) {
            e.printStackTrace();
            acknowledgementProducer.sendScanAcknowledgement(eventId, false);
        }
    }

    private void writeToJsonFile(Object data, File file) {
        try (FileWriter fw = new FileWriter(file)) {
            objectMapper.writeValue(fw, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
