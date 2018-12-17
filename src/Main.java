import jbotsim.Topology;
import jbotsim.Point;
import jbotsimx.ui.JViewer;
import java.util.Random;
import java.util.ArrayList;
import static constant.ConstEnvironment.*;

public class Main {
    final static int NODE_NUM = 5;
    public static void main(String[] args) {
        Topology tp = new Topology();
        JViewer jv = new JViewer(tp);
        jv.getJTopology().addBackgroundPainter(new BackgroundPainter());
        Random rand = new Random();
        ArrayList<Point> initPoint = new ArrayList<>();
        for (int i = 0; i < NODE_NUM; i++) {
            // avoid the same initial point
            double x, y;
            do {
                x = rand.nextInt(GRID_SIZE_X)*CELL_SIZE_X;
                y = rand.nextInt(GRID_SIZE_Y)*CELL_SIZE_Y;
            } while (initPoint.contains(new Point(x, y)));
            initPoint.add(new Point(x, y));
            tp.addNode(x, y, new GridNode());
        }
        tp.setClockSpeed(50);
        tp.setCommunicationRange(1000);
        tp.start();
    }
}
