package com.example.capstone.githubtools.model;

public interface Event<T> {

    EventTypes getType();
    T getPayload();
    String getEventId();
}
