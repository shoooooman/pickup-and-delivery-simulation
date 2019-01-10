package constant;

public class ConstExperiment {
    private ConstExperiment() {}
    public final static boolean EXPERIMENT = true;
    public final static int CLOCK_NUM = 1000;
    public final static int RUN_NUM = 2;
    public final static int DELAY_AVERAGES[] = {10, 20};
    public final static int WINDOW_SIZES[] = {2, 3, 5};
    public final static int NODE_NUMS[] = {2, 5, 10};
    public final static String FILE_PATH = "log/out.xlsx";
}
