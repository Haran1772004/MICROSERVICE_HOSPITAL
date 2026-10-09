package com.hospital.hospitalservice.dto;

import java.io.Serializable;

/**
 * A simple text answer.
 *
 * @param message the text for the caller
 */
public record MessageResponse(String message) implements Serializable {
}
