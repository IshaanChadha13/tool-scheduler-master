package com.example.capstone.githubtools.dto;

import com.example.capstone.githubtools.model.Event;
import com.example.capstone.githubtools.model.EventTypes;
import com.example.capstone.githubtools.model.ScanMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanMessageEvent implements Event<ScanMessage> {

    private String eventId;
    private ScanMessage payload;

    public ScanMessageEvent(ScanMessage payload) {
        this.payload = payload;
    }

    public ScanMessageEvent() {

    }


    public void setPayload(ScanMessage payload) {
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public EventTypes getType() {
        return EventTypes.SCAN_PULL;
    }

    @Override
    public ScanMessage getPayload() {
        return payload;
    }

}
