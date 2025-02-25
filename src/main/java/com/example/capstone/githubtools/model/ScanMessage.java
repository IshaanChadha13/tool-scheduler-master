package com.example.capstone.githubtools.model;

public class ScanMessage {

    private Long tenantId;   // <--- newly added
    private String tool;

    public Long getTenantId() {
        return tenantId;
    }
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }
}
