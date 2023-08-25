package dev.wbell.terrariateleporter;

public class PositionData {
    private final double x;
    private final double y;
    private final double z;

    public PositionData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Getters and setters (or use public fields) for the variables
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }
}