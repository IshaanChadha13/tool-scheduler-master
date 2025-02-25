package com.example.capstone.githubtools.dto;

import com.example.capstone.githubtools.model.Acknowledgement;
import com.example.capstone.githubtools.model.AcknowledgementEvent;

import java.util.UUID;

public class ScanAcknowledgement implements Acknowledgement<AcknowledgementEvent> {

    private String acknowledgementId;
    private AcknowledgementEvent payload;

    public ScanAcknowledgement() {}
    public ScanAcknowledgement(String acknowledgementId, AcknowledgementEvent payload) {
        this.acknowledgementId = (acknowledgementId == null || acknowledgementId.isEmpty()) ? UUID.randomUUID().toString() : acknowledgementId;
        this.payload = payload;
    }
    public void setAcknowledgementId(String acknowledgementId) {
        this.acknowledgementId = acknowledgementId;
    }
    public void setPayload(AcknowledgementEvent payload) {
        this.payload = payload;
    }
    @Override
    public String getAcknowledgementId() {
        return acknowledgementId;
    }
    @Override
    public AcknowledgementEvent getPayload() {
        return payload;
    }
}
