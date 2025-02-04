package com.example.capstone.githubtools.controller;

import com.example.capstone.githubtools.service.ToolSchedulerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final ToolSchedulerService toolSchedulerService;

    public SchedulerController(ToolSchedulerService toolSchedulerService) {
        this.toolSchedulerService = toolSchedulerService;
    }

    /**
     * The new endpoint that the React "Scan" button calls:
     * e.g. POST /scheduler/scan?owner=IshaanChadha13&repo=juice-shop
     */
//    @PostMapping("/scan")
//    public String scanRepository(@RequestParam String owner,
//                                 @RequestParam String repo) {
//        toolSchedulerService.processRepository(owner, repo);
//        return "Scan triggered for " + owner + "/" + repo;
//    }
}
