import jbotsim.Point;
import jbotsimx.ui.JViewer;
import jbotsimx.ui.JTopology;
import jbotsimx.messaging.AsyncMessageEngine;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;

public class Main {
    public static void main(String[] args) {
        MyTopology tp = new MyTopology();
        JTopology jtp = new JTopology(tp);
        // remove links among nodes
        jtp.disableDrawings();
        JViewer jv = new JViewer(jtp);
        jv.getJTopology().addBackgroundPainter(new PathPainter());
        jv.getJTopology().addBackgroundPainter(new GridPainter());
        tp.deployNodes();
        tp.setClockSpeed(CLOCK_SPEED);
        tp.setCommunicationRange(COMMUNICATION_RANGE);
        tp.setMessageEngine(new AsyncMessageEngine(tp.getDelay(), AsyncMessageEngine.Type.NONFIFO));
        tp.start();
    }
}
