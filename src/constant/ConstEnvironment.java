package constant;

public final class ConstEnvironment {
    private ConstEnvironment() {}

    // Grid
    public static final int GRID_SIZE_X = 6;
    public static final int GRID_SIZE_Y = 4;
    public static final int CELL_SIZE_X = 50;
    public static final int CELL_SIZE_Y = 50;

    // Topology
    public static final int CLOCK_SPEED = 0;
    public static final int COMMUNICATION_RANGE = 1000;
    public static final int DELAY_AVERAGE = 5;

    // For Delay
    // Exponential Distribution
    public static final double EXPONENTIAL_LAMBDA = 1.0 / DELAY_AVERAGE;
    // Pareto Distribution
    public static final double PARETO_X_M = 1.0;
    public static final double PARETO_K = ((double) DELAY_AVERAGE) / (DELAY_AVERAGE-1);
}
