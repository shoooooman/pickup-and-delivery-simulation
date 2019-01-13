package constant;

public class ConstExperiment {
    private ConstExperiment() {}
    public final static boolean DEBUG = false;
    public final static boolean EXPERIMENT = true;
    public final static int CLOCK_NUM = 10000;
    public final static int RUN_NUM = 100;
    public final static boolean PRIORITY[] = {true, false};
    public final static int DELAY_AVERAGES[] = {10};
    public final static int WINDOW_SIZES[] = {2, 3, 4, 5, 6};
    public final static int NODE_NUMS[] = {1, 2, 4, 8, 12, 16};
    public final static String FILE_PATH = "log/out.xlsx";
    public final static String FILE_NAME_HEAD = "data";
}
