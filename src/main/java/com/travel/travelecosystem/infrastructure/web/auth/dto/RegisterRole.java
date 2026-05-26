package com.travel.travelecosystem.infrastructure.web.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.travel.travelecosystem.infrastructure.persistence.entity.UserEntity;

/**
 * Роли, доступные при публичной регистрации. ROLE_ADMIN недоступна.
 */
public enum RegisterRole {

    @JsonProperty("TRAVELER")
    @JsonAlias({"TOURIST", "ROLE_TRAVELER"})
    TRAVELER,

    @JsonProperty("OPERATOR")
    @JsonAlias({"TOUR_OPERATOR", "ROLE_OPERATOR"})
    OPERATOR;

    public UserEntity.UserRole toEntityRole() {
        return switch (this) {
            case TRAVELER -> UserEntity.UserRole.ROLE_TRAVELER;
            case OPERATOR -> UserEntity.UserRole.ROLE_OPERATOR;
        };
    }
}
