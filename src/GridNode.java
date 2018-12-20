import jbotsim.Node;
import jbotsim.Point;
import jbotsim.Color;
import jbotsim.Message;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;

public class GridNode extends AbstractGridNode {
    @Override
    public void onClock() {
        if (done)
            this.setColor(Color.green);
        else
            this.setColor(Color.blue);

        GridPoint here = this.getLocation();

        // when reaching the destination
        if (!done && here.equals(dest)) {
            /* for debugging */
            GRID_LOG("complete!");
            /* end debugging */

            prev = dest;
            // when completing the task, the node is regarded as absence
            // locking.remove(next);
            locking.clear();
            done = true;

            // this.setColor(Color.green);
            // // done = false;
            // PathGenerator pathGen = new PathGenerator();
            // path = pathGen.newPath(this, this.getLocation());
            // if (!path.isEmpty()) {
            //     dest = path.getLast();
            // } else {
            //     dest = getLocation();
            // }
        }

        if (!done && !waiting) {
            acceptedLocks = MAX_LOCKS;
            sendRequest();
            waitingFrom = (ArrayList<Node>) this.getNeighbors();
            waiting = true; // wait for replies from all the other nodes
        }
        if (!done && waiting) {
            // when got replies from all the other nodes
            if (waitingFrom.isEmpty()) {

                /* for debugging */
                GRID_LOG("acceptedLocks=" + acceptedLocks);
                /* end debugging */

                for (int i = 0; i < acceptedLocks; i++) {
                    if (requesting.isEmpty()) break;
                    GridPoint point = requesting.remove();
                    locking.add(point);
                    numOfReq.remove(point);
                }

                waiting = false;
            } else {
                /* for debugging */
                GRID_LOG("waitingFrom ", false);
                for (Node node : waitingFrom)
                    System.out.print(node.getID() + " ");
                System.out.println();
                /* end debugging */
            }
        }

        // when arriving at a grid point
        if (!done && here.equals(next)) {
            this.setColor(Color.red);

            /* for debugging */
            GRID_LOG(here);
            GRID_LOG("locking ", false);
            for (GridPoint p : locking) {
                System.out.print(p);
            }
            System.out.println();
            /* end debugging */

            // when staying prev can be equal next
            if (!here.equals(prev)) {
                assert(!prev.equals(next));

                boolean released = locking.remove(prev);
                /* for debugging */
                if (released)
                    GRID_LOG("released " + prev);
                /* end debugging */

                prev = next;
            }

            // here "2" means current (or previous) point and the next point to move
            if (locking.size() >= 2) {
                Iterator<GridPoint> ite = locking.iterator();
                GridPoint p = ite.next();
                assert(p.equals(prev)); // the head of locking is current or previous point
                next = ite.next(); // next point to move
                stay = false;
                setDirection(next);
            } else {
                // when can get no locks other than current point

                /* for debugging */
                GRID_LOG("staying");
                /* end debugging */

                if (!requesting.isEmpty())
                    setDirection(requesting.element());

                stay = true;
            }
        }
    }
}
