package org.policedog.registry.domain;

import lombok.Getter;

@Getter
public enum Status {
    TRAINING("Training"),
    IN_SERVICE("In Service"),
    RETIRED("Retired"),
    LEFT("Left");

    private final String description;

    Status(String description) {
        this.description = description;
    }
}
