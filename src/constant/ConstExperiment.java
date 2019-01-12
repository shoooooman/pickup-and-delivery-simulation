package constant;

public class ConstExperiment {
    private ConstExperiment() {}
    public final static boolean DEBUG = false;
    public final static boolean EXPERIMENT = true;
    public final static int CLOCK_NUM = 10000;
    public final static int RUN_NUM = 100;
    public final static boolean PRIORITY[] = {true, false};
    public final static int DELAY_AVERAGES[] = {5, 10, 20, 25, 40, 50, 75, 100};
    public final static int WINDOW_SIZES[] = {2, 3, 4, 5, 6, 7};
    public final static int NODE_NUMS[] = {1, 2, 4, 8, 16, 24, 32, 48};
    public final static String FILE_PATH = "log/out.xlsx";
}
