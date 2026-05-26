package com.travel.travelecosystem.domain.model;

/**
 * Темп маршрута (не хранится на местах, только для конструктора).
 */
public enum RoutePace {
    CALM(3),
    MODERATE(4),
    INTENSIVE(5);

    private final int stopCount;

    RoutePace(int stopCount) {
        this.stopCount = stopCount;
    }

    public int getStopCount() {
        return stopCount;
    }
}
