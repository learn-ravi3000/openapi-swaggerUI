package com.example.service.model;

/**
 * Possible health outcomes produced by heartbeat checks for a service.
 */
public enum ServiceStatus {
    UNKNOWN,
    UP,
    DEGRADED,
    DOWN
}
