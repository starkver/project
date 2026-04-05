package com.rogue.domain.combat;

public class Buff {
    private String stat;
    private int value;
    private int duration;

    public Buff(String stat, int value, int duration) {
        this.stat = stat;
        this.value = value;
        this.duration = duration;
    }

    public void tick() {
        duration--;
    }

    public boolean isExpired() {
        return duration <= 0;
    }

    public String getStat() { return stat; }
    public int getValue() { return value; }
}