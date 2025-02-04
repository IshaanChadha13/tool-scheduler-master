package com.example.capstone.githubtools.service;

import com.example.capstone.githubtools.githubclient.GithubApiClient;
import com.example.capstone.githubtools.repository.RepoTokenMappingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ToolSchedulerService {

    private final RepoTokenMappingRepository tokenMappingRepo;
    private final GithubApiClient githubApiClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.parser-topic}")
    private String parserTopic;

    @Value("${myapp.local-storage}")
    private String localStoragePath;

    public ToolSchedulerService(
            RepoTokenMappingRepository tokenMappingRepo,
            GithubApiClient githubApiClient,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.tokenMappingRepo = tokenMappingRepo;
        this.githubApiClient = githubApiClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void processRepository(String owner, String repo, List<String> tools) {
        // 1) Fetch the token from DB
        var mappingOpt = tokenMappingRepo.findByOwnerAndRepository(owner, repo);
        if (mappingOpt.isEmpty()) {
            System.out.println("No PAT found for " + owner + "/" + repo);
            return;
        }

        String pat = mappingOpt.get().getPersonalAccessToken();

        // 2) Create the base directory (owner-repo folder) if not existing
        String ownerRepoFolder = localStoragePath + File.separator + owner + "-" + repo;
        File baseDir = new File(ownerRepoFolder);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        // 3) Fetch/store alerts ONLY for selected tools
        if (tools.contains("CODE_SCANNING")) {
            var codeAlerts = githubApiClient.fetchCodeScanningAlerts(owner, repo, pat);
            writeAndSendToParser(owner, repo, "CODE_SCANNING", codeAlerts);
        }

        if (tools.contains("DEPENDABOT")) {
            var dependabotAlerts = githubApiClient.fetchDependabotAlerts(owner, repo, pat);
            writeAndSendToParser(owner, repo, "DEPENDABOT", dependabotAlerts);
        }

        if (tools.contains("SECRET_SCANNING")) {
            var secretAlerts = githubApiClient.fetchSecretScanningAlerts(owner, repo, pat);
            writeAndSendToParser(owner, repo, "SECRET_SCANNING", secretAlerts);
        }

        // If the user didn't choose any tools, do nothing (or log it)
        if (tools.isEmpty()) {
            System.out.println("No tools were selected for " + owner + "/" + repo);
        }
    }

    private void writeAndSendToParser(String owner, String repo, String toolType,
                                      List<Map<String,Object>> alerts) {
        // subfolder => e.g. "/tmp/alerts/IshaanChadha13-juice-shop/CODE_SCANNING"
        String subFolderPath = localStoragePath
                + File.separator + owner + "-" + repo
                + File.separator + toolType;
        File subFolder = new File(subFolderPath);
        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }

        // file name => e.g. "IshaanChadha13-juice-shop-CODE_SCANNING-<timestamp>.json"
        String fileName = owner + "-" + repo + "-" + toolType
                + "-" + System.currentTimeMillis() + ".json";
        File outFile = new File(subFolder, fileName);

        // Write the alerts to that file
        writeToJsonFile(alerts, outFile);

        // Publish the file path to parser topic
        String filePath = outFile.getAbsolutePath();
        kafkaTemplate.send(parserTopic, filePath);
        System.out.println("Saved " + toolType + " alerts to: " + filePath);
    }

    private void writeToJsonFile(Object data, File file) {
        ObjectMapper mapper = new ObjectMapper();
        try (FileWriter fw = new FileWriter(file)) {
            mapper.writeValue(fw, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
