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

    // points that this node will move
    // almost same as locking, but pointsToMove does not include
    // nodes that this node have already passed
    ArrayDeque<GridPoint> pointsToMove = new ArrayDeque<>();

    SimpleGridNode() {
        nodeType = NodeType.Simple;
    }

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
                    pointsToMove.add(point);
                    numOfReq.remove(point);
                }

                waiting = false;
            } else {
                /* for debugging */
                GRID_LOG("waitingFrom ", true, false);
                for (Node node : waitingFrom)
                    GRID_LOG(node.getID() + " ", false, false);
                GRID_LOG();
                /* end debugging */
            }
        }


        // when arriving at a grid point
        if (here.equals(next)) {

            /* for debugging */
            GRID_LOG(here);
            GRID_LOG("locking ", true, false);
            for (GridPoint p : locking) {
                GRID_LOG(p, false, false);
            }
            GRID_LOG();
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

                // the number of tasks this node completed
                numTask++;

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

                pointsToMove.remove(next);
                prev = next;
            }


            if (!pointsToMove.isEmpty()) {
                next = pointsToMove.element();
                stay = false;
                setDirection(next);
            } else {
                // when can get no locks other than current point

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
        if (!waiting && pointsToMove.isEmpty() && (!requesting.isEmpty() || !path.isEmpty())) {
            locking.clear();
            locking.add(prev); // current or previous point
            // the max number of points this node can request at once
            // 1 represents previous or current point
            int pathLength = requesting.size() + path.size() + 1;
            acceptedLocks = pathLength;
            sendRequest(pathLength);
            waitingFrom = (ArrayList<Node>) this.getNeighbors();
            waiting = true; // wait for replies from all the other nodes
        }
    }
}
