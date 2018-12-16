import jbotsim.Topology;
import jbotsimx.ui.JViewer;
import java.util.Random;
import static constant.ConstEnvironment.*;

public class Main {
    final static int NODE_NUM = 15;
    public static void main(String[] args) {
        Topology tp = new Topology();
        JViewer jv = new JViewer(tp);
        jv.getJTopology().addBackgroundPainter(new BackgroundPainter());
        Random rand = new Random();
        for (int i = 0; i < NODE_NUM; i++) {
            tp.addNode(rand.nextInt(GRID_SIZE_X)*CELL_SIZE_X, rand.nextInt(GRID_SIZE_Y)*CELL_SIZE_Y, new GridNode());
        }
        tp.setClockSpeed(50);
        tp.start();
    }
}
