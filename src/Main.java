import jbotsim.Point;
import jbotsimx.ui.JViewer;
import jbotsimx.ui.JTopology;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;

public class Main {
    public static void main(String[] args) {
        MyTopology tp = new MyTopology(GRID_SIZE_X*CELL_SIZE_X, GRID_SIZE_Y*CELL_SIZE_Y);
        JTopology jtp = new JTopology(tp);
        // remove links among nodes
        jtp.disableDrawings();
        JViewer jv = new JViewer(jtp);
        jv.getJTopology().addBackgroundPainter(new PathPainter());
        jv.getJTopology().addBackgroundPainter(new GridPainter());
        tp.deployNodes();
        tp.setClockSpeed(CLOCK_SPEED);
        tp.setCommunicationRange(COMMUNICATION_RANGE);
        tp.setMessageEngine(new ExponentialAsyncMessageEngine(tp.getDelay(), ExponentialAsyncMessageEngine.Type.NONFIFO));
        tp.start();
    }
}
