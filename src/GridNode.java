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
    GridPoint evacuationPoint;
    @Override
    public void onClock() {
        if (conceding)
            this.setColor(Color.gray);
        else
            this.setColor(Color.blue);

        GridPoint here = this.getLocation();

        if (waiting) {
            // when got replies from all the other nodes
            if (waitingFrom.isEmpty()) {

                // when the node for evacuation is accepted
                if (conceding && acceptedLocks == 1) {
                    numOfAvoid = 0;
                }

                /* for debugging */
                GRID_LOG("acceptedLocks=" + acceptedLocks);
                /* end debugging */

                for (int i = 0; i < acceptedLocks; i++) {
                    if (requesting.isEmpty()) break;
                    GridPoint point = requesting.remove();
                    locking.add(point);
                    numOfReq.remove(point);
                }

                /* for debugging */
                GRID_LOG("locking ", false);
                for (GridPoint p : locking) {
                    System.out.print(p);
                }
                System.out.println();
                /* end debugging */

                waiting = false;
            } else {
                /* for debugging */
                // GRID_LOG("waitingFrom ", false);
                // for (Node node : waitingFrom)
                //     System.out.print(node.getID() + " ");
                // System.out.println();
                /* end debugging */
            }
        }

        // when arriving at a grid point
        if (here.equals(next)) {

            /* for debugging */
            GRID_LOG(here);
            GRID_LOG("locking ", false);
            for (GridPoint p : locking) {
                System.out.print(p);
            }
            System.out.println();
            /* end debugging */

            // when reach evacuation point
            if (conceding && here.equals(evacuationPoint)) {
                // get path from the evacuation point to the original destination
                path = pathGen.newPath(getID(), evacuationPoint, dest);
                conceding = false;
            }

            // when reaching the destination
            if (here.equals(dest)) {
                /* for debugging */
                GRID_LOG("complete!");
                /* end debugging */

                this.setColor(Color.green);

                path = pathGen.newPath(getID(), this.getLocation());
                if (!path.isEmpty()) {
                    dest = path.getLast();
                } else {
                    dest = getLocation();
                }
            } else if (!conceding) {
                this.setColor(Color.red);
            }

            // when not staying
            // (prev can equal next when staying)
            if (!here.equals(prev)) {
                assert(!prev.equals(next));

                boolean released = locking.remove(prev);
                /* for debugging */
                // if (released)
                //     GRID_LOG("released " + prev);
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
                assert(locking.size() == 1);

                /* for debugging */
                // GRID_LOG("staying");
                /* end debugging */

                // when got replies from all the other nodes
                if (!waiting) {
                    assert(waitingFrom.isEmpty());
                    boolean deadlock;
                    int anotherId = getIdByLocation(requesting.peek());
                    if (anotherId == -1) // when there is no node on requesting.peek()
                        deadlock = false;
                    else
                        deadlock = checkDeadlock(anotherId);

                    // calculate which node should avoid
                    if (deadlock) {
                        // "another" means the node next to this node
                        if (numOfAvoid < otherNumOfAvoid.get(anotherId)) {
                            GRID_LOG("avoid by numOfAvoid");
                            avoid = true;
                        } else if (numOfAvoid == otherNumOfAvoid.get(anotherId)) {
                            if (getID() > anotherId) {
                                GRID_LOG("avoid by ID");
                                avoid = true;
                            } else {
                                GRID_LOG("not avoid by ID");
                                avoid = false;
                            }
                        } else {
                            GRID_LOG("not avoid by numOfAvoid");
                            avoid = false;
                        }
                    }

                    // when this node must concede
                    // find direction to evacuate and set it to path
                    if (avoid) {
                        assert(acceptedLocks == 0);
                        GRID_LOG("avoiding");
                        numOfAvoid++;
                        avoid = false;
                        conceding = true;
                        this.setColor(Color.gray);
                        evacuationPoint = this.getRandomPoint(requesting.element());
                        setDirection(evacuationPoint);
                        requesting.clear();
                        numOfReq.clear();
                        path.clear();
                        path.add(evacuationPoint); // request only for evacuationPoint
                    }
                }

                if (!requesting.isEmpty())
                    setDirection(requesting.element());

                stay = true;
            }
        }

        // when have node(s) to request
        if (!waiting && (!requesting.isEmpty() || !path.isEmpty())) {
            acceptedLocks = MAX_LOCKS;
            sendRequest();
            waitingFrom = (ArrayList<Node>) this.getNeighbors();
            waiting = true; // wait for replies from all the other nodes
        }
    }
}
