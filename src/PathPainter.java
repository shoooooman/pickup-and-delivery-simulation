import jbotsim.Topology;
import jbotsim.Node;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;

public class PathPainter implements jbotsimx.ui.painting.BackgroundPainter {
    @Override
    public void paintBackground(Graphics2D g, Topology tp) {
        g.setColor(Color.magenta);
        BasicStroke BStroke = new BasicStroke(5.0f); // set line width
        g.setStroke(BStroke);

        ArrayList<Node> nodes = (ArrayList<Node>) tp.getNodes();
        for (Node node : nodes) {
            AbstractGridNode gnode = (AbstractGridNode) node;
            ArrayDeque<GridPoint> locking = gnode.getLocking();

            Iterator<GridPoint> ite = locking.iterator();
            if (ite.hasNext()) {
                GridPoint start = ite.next(); // current or previous point
                while (ite.hasNext()) {
                    GridPoint end = ite.next();
                    g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
                    start = end;
                }
            }
        }
    }
}
