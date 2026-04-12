package org.example.domain.model.entity;

/** Координаты на карте. Неизменяемы */
public record Position(int x, int y) {

    // Расстояние до другой точки
    public int distance(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
}
