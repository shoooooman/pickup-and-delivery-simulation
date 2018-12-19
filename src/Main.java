import jbotsim.Topology;
import jbotsim.Point;
import jbotsimx.ui.JViewer;
import jbotsimx.messaging.AsyncMessageEngine;
import java.util.Random;
import java.util.ArrayList;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;

public class Main {
    public static void main(String[] args) {
        Topology tp = new Topology();
        JViewer jv = new JViewer(tp);
        jv.getJTopology().addBackgroundPainter(new PathPainter());
        jv.getJTopology().addBackgroundPainter(new GridPainter());
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
        tp.setClockSpeed(CLOCK_SPEED);
        tp.setCommunicationRange(COMMUNICATION_RANGE);
        tp.setMessageEngine(new AsyncMessageEngine(DELAY_AVERAGE, AsyncMessageEngine.Type.NONFIFO));
        tp.start();
    }
}
