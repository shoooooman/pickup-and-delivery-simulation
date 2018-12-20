import jbotsim.Node;
import jbotsim.Point;
import jbotsim.Color;
import jbotsim.Message;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.ArrayList;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;

public class SimpleGridNode extends AbstractGridNode {
    ArrayDeque<GridPoint> pointsToMove = new ArrayDeque<>();
    SimpleGridNode() {
        this.setPLrd(true);
        this.setPDisToCs(false);
        this.setPNumOfReq(false);
    }

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
            locking.remove(next);
            pointsToMove.remove(next);
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
                    pointsToMove.add(point);
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

        if (pointsToMove.isEmpty()) {
            locking.clear();
            locking.add(prev); // current or previous point
            acceptedLocks = MAX_LOCKS;
            sendRequest();
            waitingFrom = (ArrayList<Node>) this.getNeighbors();
            waiting = true; // wait for replies from all the other nodes
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

            pointsToMove.remove(next);
            prev = next;

            if (!pointsToMove.isEmpty()) {
                next = pointsToMove.element();
                stay = false;
                setDirection(next);
            } else {
                // when can get no locks

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
