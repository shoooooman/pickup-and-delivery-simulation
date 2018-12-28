import jbotsim.Topology;
import jbotsim.Node;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;
import static constant.ConstEnvironment.*;

public class PathPainter implements jbotsimx.ui.painting.BackgroundPainter {
    @Override
    public void paintBackground(Graphics2D g, Topology tp) {
        BasicStroke BStroke = new BasicStroke(5.0f); // set line width
        g.setStroke(BStroke);

        Iterator<GridPoint> ite;
        ArrayList<Node> nodes = (ArrayList<Node>) tp.getNodes();
        for (Node node : nodes) {
            AbstractGridNode gnode = (AbstractGridNode) node;
            ArrayDeque<GridPoint> path = gnode.getPath();
            ArrayDeque<GridPoint> requesting = gnode.getRequesting();
            ArrayDeque<GridPoint> locking = gnode.getLocking();

            // paint locking
            g.setColor(Color.magenta);
            ite = locking.iterator();
            if (ite.hasNext()) {
                GridPoint start = ite.next(); // current or previous point
                while (ite.hasNext()) {
                    GridPoint end = ite.next();
                    // System.out.println("Painting locking from " + start + " to " + end);
                    g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
                    start = end;
                }
            }

            // paint requesting
            g.setColor(Color.orange);
            if (!requesting.isEmpty()) {
                ite = requesting.iterator();
                if (ite.hasNext()) {
                    GridPoint start = locking.getLast();
                    while (ite.hasNext()) {
                        GridPoint end = ite.next();
                        // System.out.println("Painting requesting from " + start + " to " + end);
                        g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
                        start = end;
                    }
                }
            }

            // paint path
            g.setColor(Color.gray);
            if (!path.isEmpty()) {
                ite = path.iterator();
                g.setColor(Color.gray);
                if (ite.hasNext()) {
                    GridPoint start;
                    // when requesting is empty, the first node should be tha last of locking
                    if ((start = requesting.peekLast()) == null) {
                        start = locking.getLast();
                    }
                    while (ite.hasNext()) {
                        GridPoint end = ite.next();
                        // System.out.println("Painting path from " + start + " to " + end);
                        g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
                        start = end;
                    }
                }
            }

            g.setColor(Color.green);
            GridPoint dest = gnode.getDestination();
            if (dest != null) {
                g.fillRect((int) dest.getX() - CELL_SIZE_X/10, (int) dest.getY() - CELL_SIZE_Y/10, CELL_SIZE_X/10 * 2, CELL_SIZE_Y/10 * 2);
            }

        }
    }
}
