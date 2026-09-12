package com.zidio.keystone.domain.enums;

public enum Priority {
    LOW(72),
    MEDIUM(24),
    HIGH(8),
    CRITICAL(2);

    private final int slaHours;

    Priority(int slaHours) {
        this.slaHours = slaHours;
    }

    public int getSlaHours() {
        return slaHours;
    }
}

