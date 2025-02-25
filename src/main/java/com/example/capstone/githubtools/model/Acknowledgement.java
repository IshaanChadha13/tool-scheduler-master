package com.example.capstone.githubtools.model;

public interface Acknowledgement<T> {

    String getAcknowledgementId();
    T getPayload();
}
