package com.example.capstone.githubtools.dto;

import com.example.capstone.githubtools.model.Event;
import com.example.capstone.githubtools.model.EventTypes;
import com.example.capstone.githubtools.model.ParserMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParserMessageEvent implements Event<ParserMessage> {

    private String eventId;
    private ParserMessage payload;

    // NEW
    private String destinationTopic;

    public ParserMessageEvent() {}

    public ParserMessageEvent(ParserMessage payload, String eventId, String destinationTopic) {
        this.payload = payload;
        this.eventId = eventId;
        this.destinationTopic = destinationTopic;
    }

    @Override
    public String getEventId() {
        return eventId;
    }
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public EventTypes getType() {
        return EventTypes.SCAN_PARSE; // remains SCAN_PARSE
    }

    @Override
    public ParserMessage getPayload() {
        return payload;
    }
    public void setPayload(ParserMessage payload) {
        this.payload = payload;
    }

    public String getDestinationTopic() {
        return destinationTopic;
    }
    public void setDestinationTopic(String destinationTopic) {
        this.destinationTopic = destinationTopic;
    }
}

