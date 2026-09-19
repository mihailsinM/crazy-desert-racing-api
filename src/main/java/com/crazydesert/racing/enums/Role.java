package com.crazydesert.racing.enums;

public enum Role {
    USER,
    ADMIN,
    SUPER_ADMIN;

    public boolean hasAdminAccess() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
}
