import jbotsim.Topology;
import jbotsim.Node;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.awt.*;

public class PathPainter implements jbotsimx.ui.painting.BackgroundPainter {
    @Override
    public void paintBackground(Graphics2D g, Topology tp) {
        g.setColor(Color.magenta);
        BasicStroke BStroke = new BasicStroke(5.0f); // set line width
        g.setStroke(BStroke);

        ArrayList<Node> nodes = (ArrayList<Node>) tp.getNodes();
        for (Node node : nodes) {
            GridNode gnode = (GridNode) node;
            ArrayDeque<GridPoint> locking = gnode.getLocking();
            // The point that the node is on is not in locking
            // so you need first to draw line between prev point
            // (maybe current point) and next point
            GridPoint start = gnode.getPrev();
            for (GridPoint end : locking) {
                g.drawLine((int) start.getX(), (int) start.getY(), (int) end.getX(), (int) end.getY());
                start = end;
            }
        }
    }
}
