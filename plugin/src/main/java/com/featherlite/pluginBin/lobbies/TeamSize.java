package com.featherlite.pluginBin.lobbies;

public class TeamSize {
    private final int min;
    private final int max;

    public TeamSize(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public String toString() {
        return "TeamSize{min=" + min + ", max=" + max + "}";
    }
}
