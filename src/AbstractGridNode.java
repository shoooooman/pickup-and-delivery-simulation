import jbotsim.Node;
import jbotsim.Point;
import jbotsim.Color;
import jbotsim.Message;
import jbotsim.Topology;
import jbotsimx.messaging.AsyncMessageEngine;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.ArrayList;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;
import static constant.ConstExperiment.*;

public abstract class AbstractGridNode extends Node {
    protected PathGenerator pathGen = new PathGenerator();
    protected ArrayDeque<GridPoint> path = new ArrayDeque<>();
    protected GridPoint next, prev, dest;
    protected boolean stay = true;
    protected boolean waiting = false;
    protected boolean avoid = false;
    protected boolean conceding = false;
    protected int numOfAvoid = 0;
    protected ArrayDeque<GridPoint> requesting = new ArrayDeque<>();
    protected ArrayDeque<GridPoint> locking = new ArrayDeque<>();
    protected int acceptedLocks;
    protected ArrayList<Node> waitingFrom = new ArrayList<>();
    protected int lrd = 0; // last request date
    protected int clk = 0; // logical clock
    protected HashMap<GridPoint, Integer> numOfReq = new HashMap<>();
    protected HashMap<Integer, GridPoint> otherLocations = new HashMap<>();
    protected HashMap<Integer, ArrayDeque<GridPoint>> otherRequestings = new HashMap<>();
    protected HashMap<Integer, Boolean> otherState = new HashMap<>();
    protected HashMap<Integer, Integer> otherNumOfAvoid = new HashMap<>();
    protected GridPoint evacuationPoint;
    protected int numTask = 0;
    protected int numStay = 0;
    protected NodeType nodeType;

    public AbstractGridNode() {
        setIcon("/icon/node.png");
        setSize(10);
    }

    @Override
    public void onStart() {
        path = pathGen.newPath(getID(), this.getLocation());
        // when init point is the same as dest, path will be empty
        if (!path.isEmpty()) {
            dest = path.getLast();
        } else {
            dest = getLocation();
        }
        next = prev = this.getLocation();
        locking.add(next);
    }
    @Override
    abstract public void onClock();

    private int getListSum(ArrayList<Integer> list) {
        int sum = 0;
        for (Integer item : list) {
            sum += item;
        }
        return sum;
    }

    private double getListVar(ArrayList<Integer> list) {
        double average = ((double) getListSum(list)) / list.size();
        double variable = 0.0;
        for (Integer item : list) {
            variable += Math.pow(item-average, 2);
        }
        variable = variable / list.size();
        return variable;
    }

    @Override
    public void onPostClock() {
        if (!stay)
            moveDir(1.0);
        else
            numStay++;

        // for experiment
        if (EXPERIMENT && getID() == 0 && getTime() >= CLOCK_NUM) {
            MyTopology tp = (MyTopology) getTopology();

            ArrayList<Integer> numStays = tp.getNumStays();
            int sumStays = getListSum(numStays);

            ArrayList<Integer> numTasks = tp.getNumTasks();
            int sumTasks = getListSum(numTasks);
            double varTasks = getListVar(numTasks);

            // for debugging
            System.out.println("(d,w,N)=(" + tp.getDelay() + "," + tp.getWindowSize() + "," + tp.getNodeNum() + ")");
            System.out.println("sum of stays=" + sumStays);
            System.out.println("sum of tasks=" + sumTasks);
            System.out.println("var of tasks=" + varTasks);
            // end debugging

            // add data to buffer
            ExcelWriter writer = tp.getExcelWriter();
            writer.addData(tp.getDataNo(), nodeType, tp.getPCs(), tp.getDelay(), tp.getWindowSize(), tp.getNodeNum(), sumStays, sumTasks, varTasks);

            tp.incDataNo();

            if (tp.incRunCounter() < RUN_NUM) {
                tp.nextTrial();
            } else {
                assert(tp.getRunCounter() == RUN_NUM);

                // add summary (average and standard error) of data to buffer
                writer.addSummary(tp.getConditionNo(), nodeType, tp.getPCs(), tp.getDelay(), tp.getWindowSize(), tp.getNodeNum());
                tp.incConditionNo();

                tp.setRunCounter(0);
                if (tp.incNIndex() < NODE_NUMS.length) {
                    tp.nextTrial();
                } else {
                    assert(tp.getNIndex() == NODE_NUMS.length);
                    tp.setNIndex(0);
                    if (tp.incWIndex() < WINDOW_SIZES.length) {
                        tp.nextTrial();
                    } else {
                        assert(tp.getWIndex() == WINDOW_SIZES.length);
                        tp.setWIndex(0);
                        if (tp.incDIndex() < DELAY_AVERAGES.length) {
                            // set new delay
                            tp.setMessageEngine(new AsyncMessageEngine(tp.getDelay(), AsyncMessageEngine.Type.NONFIFO));
                            tp.nextTrial();
                        } else {
                            assert(tp.getDIndex() == DELAY_AVERAGES.length);
                            tp.setDIndex(0);
                            if (tp.incPIndex() < PRIORITY.length) {
                                tp.nextTrial();
                            } else {
                                assert(tp.getPIndex() == PRIORITY.length);
                                // write buffer into file
                                writer.writeFile();
                                tp.pause();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMessage(Message msg) {
        @SuppressWarnings("unchecked")
        HashMap<String, Object> request = (HashMap<String, Object>) msg.getContent();
        String topic = (String)request.get("topic");

        switch(topic) {
            case "request":
                receiveRequest(msg);
                break;
            case "reply":
                receiveReply(msg);
                break;
            default:
                break;
        }
    }

    protected void GRID_LOG(Object log) {
        System.out.println("ID: " + getID() + " " + log);
    }
    protected void GRID_LOG(Object log, boolean newLine) {
        if (newLine)
            System.out.println("ID: " + getID() + " " + log);
        else
            System.out.print  ("ID: " + getID() + " " + log);
    }

    /**
     * Return exact direction from a current point to the argument point
     */
    protected double calcDirection(Point p) {
        double dx = p.getX() - this.getX();
        double dy = p.getY() - this.getY();
        int diffX = (int)(dx/CELL_SIZE_X);
        int diffY = (int)(dy/CELL_SIZE_Y);
        if (diffX == 1 && diffY == 0)
            return 0.0;
        else if (diffX == 0 && diffY == 1)
            return Math.PI/2.0;
        else if (diffX == -1 && diffY == 0)
            return Math.PI;
        else if (diffX == 0 && diffY == -1)
            return -Math.PI/2.0;
        else {
            System.out.println("Cannot calculate direction");
            return 0.0;
        }
    }

    /**
     * sin(pi) などの誤差によって正しい方向に移動しない
     * directionから直接移動する場所を指定
     */
    public void moveDir(double distance) {
        double direction = this.getDirection();
        if (direction == 0.0)
            translate(1.0*distance, 0.0*distance);
        else if (direction == Math.PI/2.0)
            translate(0.0*distance, 1.0*distance);
        else if (direction == Math.PI)
            translate(-1.0*distance, 0.0*distance);
        else if (direction == -Math.PI/2.0)
            translate(0.0*distance, -1.0*distance);
        else {
            translate(Math.cos(direction)*distance, Math.sin(direction)*distance);
            System.out.println("else!");
        }
    }

    /**
     * Decide a random next point other than forbidden
     */
    public GridPoint getRandomPoint(GridPoint forbidden) {
        double x = getX();
        double y = getY();
        double fx = forbidden.getX();
        double fy = forbidden.getY();
        final int North = 0;
        final int East  = 1;
        final int South = 2;
        final int West  = 3;
        GRID_LOG("getRandomPoint: (x,y)=" + getLocation() + ", (fx,fy)=" + forbidden);
        double rand;
        int randDir = -1;
        if (x == 0.0 && y == 0.0) { // top left corner
            if      (fx == x+CELL_SIZE_X && fy == y)
                randDir = South;
            else if (fx == x && fy == y+CELL_SIZE_Y)
                randDir = East;
            else
                GRID_LOG("error: wrong position");
        } else if (x == 0.0 && y == CELL_SIZE_Y*GRID_SIZE_Y) { // bottom left corner
            if      (fx == x+CELL_SIZE_X && fy == y)
                randDir = North;
            else if (fx == x && fy == y-CELL_SIZE_Y)
                randDir = East;
            else
                GRID_LOG("error: wrong position");
        } else if (x == CELL_SIZE_X*GRID_SIZE_X && y == CELL_SIZE_Y*GRID_SIZE_Y) { // bottom right corner
            if      (fx == x-CELL_SIZE_X && fy == y)
                randDir = North;
            else if (fx == x && fy == y-CELL_SIZE_Y)
                randDir = West;
            else
                GRID_LOG("error: wrong position");
        } else if (x == CELL_SIZE_X*GRID_SIZE_X && y == 0.0) { // top right corner
            if      (fx == x-CELL_SIZE_X && fy == y)
                randDir = South;
            else if (fx == x && fy == y+CELL_SIZE_Y)
                randDir = West;
            else
                GRID_LOG("error: wrong position");
        } else if (x == 0) { // left edge
            rand = Math.random()*2;
            if        (fx == x && fy == y-CELL_SIZE_Y) {
                if (rand < 1.0) randDir = South;
                else            randDir = East;
            } else if (fx == x+CELL_SIZE_X && fy == y) {
                if (rand < 1.0) randDir = North;
                else            randDir = South;
            } else if (fx == x && fy == y+CELL_SIZE_Y){
                if (rand < 1.0) randDir = North;
                else            randDir = East;
            } else {
                GRID_LOG("error: wrong position");
            }
        } else if (y == CELL_SIZE_Y*GRID_SIZE_Y) { // bottom edge
            rand = Math.random()*2;
            if        (fx == x && fy == y-CELL_SIZE_Y) {
                if (rand < 1.0) randDir = West;
                else            randDir = East;
            } else if (fx == x-CELL_SIZE_X && fy == y) {
                if (rand < 1.0) randDir = North;
                else            randDir = East;
            } else if (fx == x+CELL_SIZE_Y && fy == y){
                if (rand < 1.0) randDir = North;
                else            randDir = West;
            } else {
                GRID_LOG("error: wrong position");
            }
        } else if (x == CELL_SIZE_X*GRID_SIZE_X) { // right edge
            rand = Math.random()*2;
            if (fx == x && fy == y-CELL_SIZE_Y) {
                if (rand < 1.0) randDir = West;
                else            randDir = South;
            } else if (fx == x-CELL_SIZE_X && fy == y) {
                if (rand < 1.0) randDir = North;
                else            randDir = South;
            } else if (fx == x && fy == y+CELL_SIZE_Y) {
                if (rand < 1.0) randDir = North;
                else            randDir = West;
            } else {
                GRID_LOG("error: wrong position");
            }
        } else if (y == 0) { // top edge
            rand = Math.random()*2;
            if (fx == x-CELL_SIZE_X && fy == y) {
                if (rand < 1.0) randDir = South;
                else            randDir = East;
            } else if (fx == x && fy == y+CELL_SIZE_Y) {
                if (rand < 1.0) randDir = West;
                else            randDir = East;
            } else if (fx == x+CELL_SIZE_X && fy == y) {
                if (rand < 1.0) randDir = West;
                else            randDir = South;
            } else {
                GRID_LOG("error: wrong position");
            }
        } else { // otherwise
            rand = Math.random()*3;
            if (fx == x && fy == y-CELL_SIZE_Y) {
                if      (rand < 1.0) randDir = West;
                else if (rand < 2.0) randDir = South;
                else                 randDir = East;
            } else if (fx == x-CELL_SIZE_X && fy == y) {
                if      (rand < 1.0) randDir = North;
                else if (rand < 2.0) randDir = South;
                else                 randDir = East;
            } else if (fx == x && fy == y+CELL_SIZE_Y) {
                if      (rand < 1.0) randDir = North;
                else if (rand < 2.0) randDir = West;
                else                 randDir = East;
            } else if (fx == x+CELL_SIZE_X && fy == y) {
                if      (rand < 1.0) randDir = North;
                else if (rand < 2.0) randDir = West;
                else                 randDir = South;
            } else {
                GRID_LOG("error: wrong position");
            }
        }

        switch(randDir) {
            case North:
                return new GridPoint(x, y-CELL_SIZE_Y);
            case East:
                return new GridPoint(x+CELL_SIZE_X, y);
            case South:
                return new GridPoint(x, y+CELL_SIZE_Y);
            case West:
                return new GridPoint(x-CELL_SIZE_X, y);
            default:
                GRID_LOG("error: getRandomPoint");
                return null;
        }
    }

    /**
     * Update a requesting point list with path
     * then send a request message to all the other nodes
     */
    protected void sendRequest(int maxLocks) {
        while (requesting.size() + locking.size() < maxLocks) {
            if (path.isEmpty()) break;
            requesting.add(path.remove());
        }
        lrd = clk + 1;
        for (GridPoint point : requesting) {
            int norq = numOfReq.getOrDefault(point, 0);
            if (norq == 0)
                numOfReq.put(point, 1);
            else
                numOfReq.replace(point, norq+1);
        }

        // Update this node's data
        otherLocations.put(getID(), getLocation());
        otherRequestings.put(getID(), requesting);
        otherState.put(getID(), stay);
        otherNumOfAvoid.put(getID(), numOfAvoid);

        HashMap<String, Object> content = new HashMap<>();
        content.put("topic", "request");
        content.put("location", getLocation());
        content.put("requestingPoints", requesting.clone());
        content.put("stay", stay);
        content.put("numOfAvoid", numOfAvoid);
        content.put("lrd", lrd);
        content.put("numOfReq", numOfReq);
        Object obj = (Object) content;
        Message msg = new Message(content);
        GRID_LOG("sending request for: ", false);
        for (GridPoint point : requesting) {
            System.out.print(point + " ");
        }
        System.out.println();
        sendAll(msg);
    }

    protected void sendRequest() {
        int maxLocks = ((MyTopology) getTopology()).getWindowSize();
        sendRequest(maxLocks);
    }

    public AbstractGridNode getNeighborByLocation(GridPoint location) {
        ArrayList<Node> nodes = (ArrayList<Node>) this.getNeighbors();
        for (Node node : nodes) {
            AbstractGridNode gnode = (AbstractGridNode) node;
            if (gnode.isOnGridPoint() && gnode.getLocation().equals(location))
                return gnode;
        }
        return null;
    }

    /**
     * Return ID which locates on the param
     * return -1 when there is no node on the param
     */
    public int getIdByLocation(GridPoint location) {
        for (int key : otherLocations.keySet()) {
            GridPoint val = otherLocations.get(key);
            double xmod = val.getX() % (double) CELL_SIZE_X;
            double ymod = val.getY() % (double) CELL_SIZE_Y;
            if (xmod == 0.0 && ymod == 0.0 && val.equals(location)) {
                return key;
            }
        }
        return -1;
    }

    /**
     * Detect deadlock(circle)
     * checking starts with this node and the next node
     */
    protected boolean checkDeadlock(int anotherId) {
        // check first the relationship between this node and the next node
        GridPoint thisFirstReqPoint = requesting.peek();
        ArrayDeque<GridPoint> anotherReqs = otherRequestings.get(anotherId);
        GridPoint anotherFirstReqPoint;
        if (anotherReqs != null)
            anotherFirstReqPoint = anotherReqs.peek();
        else // before receiving the first request
            anotherFirstReqPoint = null;

        if (thisFirstReqPoint == null || anotherFirstReqPoint == null)
            return false;

        GridPoint anotherLocation = otherLocations.get(anotherId);
        assert(anotherLocation != null);
        // when intersection occurs
        if (thisFirstReqPoint.equals(anotherLocation)
                && getLocation().equals(anotherFirstReqPoint))
            return true;


        // check next the relationships starting with the another node
        // and check whether circle exists recursively
        GridPoint firstPoint = anotherFirstReqPoint;
        int nodeIdOnFirstPoint = anotherId;
        // GridPoint firstPoint = thisFirstReqPoint;
        // int nodeIdOnFirstPoint = getID();

        ArrayList<Integer> checkedId = new ArrayList<>();
        // checkedId.add(anotherId);

        while (true) {
            // tmp for debugging
            int tmpId = nodeIdOnFirstPoint;
            nodeIdOnFirstPoint = getIdByLocation(firstPoint);
            if (nodeIdOnFirstPoint != -1) {
                GRID_LOG("checking deadlock between " + tmpId + " and " + nodeIdOnFirstPoint);
            }
            // boolean nodeOnFirstNodeIsStay = otherState.get(getIdByLocation);

            if (nodeIdOnFirstPoint == -1) {
                // when there is no node next to previous nodeIdOnFirstPoint
                GRID_LOG("ID: " + tmpId + " can move to the next requesting point");
                return false;
            } else if (!otherState.get(nodeIdOnFirstPoint)){
                // when just passing the next point,
                // it does not mean there is deadlock
                return false;
            } else if (nodeIdOnFirstPoint == this.getID()) {
                GRID_LOG("Detect deadlock with" + tmpId);
                return true;
            } else if (checkedId.contains(nodeIdOnFirstPoint)) {
                // although circle(deadlock) exists,
                // this is not part of it
                GRID_LOG("This node is not included in a circle");
                return false;
            } else {
                ArrayDeque<GridPoint> nodeOnFirstNodeReqs = otherRequestings.get(nodeIdOnFirstPoint);
                if (nodeOnFirstNodeReqs != null)
                    firstPoint = otherRequestings.get(nodeIdOnFirstPoint).peek();
                else
                    firstPoint = null;

                if (firstPoint == null) {
                    GRID_LOG("ID: " + nodeIdOnFirstPoint + " has no firstPoint");
                    return false;
                }
                checkedId.add(nodeIdOnFirstPoint);
            }
        }
    }

    /**
     * Detect only intersection (to be removed)
     */
    protected boolean checkDeadlock() {
        GridPoint thisFirstReqPoint = requesting.peek();
        if (thisFirstReqPoint == null)
            return false;

        AbstractGridNode another = getNeighborByLocation(thisFirstReqPoint);
        if (another == null)
            return false;

        GridPoint anotherFirstReqPoint = another.getRequesting().peek();
        if (anotherFirstReqPoint == null)
            return false;

        if (thisFirstReqPoint.equals(another.getLocation()) && getLocation().equals(anotherFirstReqPoint))
            return true;
        else
            return false;
    }

    /**
     * Called when receiving a message with topic "request"
     * then caliculates the priorities for all requesting points
     * send back a maximum number which this node can accept to be locked from sender
     */
    protected void receiveRequest(Message msg) {
        AbstractGridNode sender = (AbstractGridNode) msg.getSender();
        final int senderId = sender.getID();

        /* for debugging */
        // GRID_LOG("received request from " + sender.getID());
        /* end debugging */

        @SuppressWarnings("unchecked")
        HashMap<String, Object> request = (HashMap<String, Object>) msg.getContent();

        // update other's informations
        @SuppressWarnings("unchecked")
        GridPoint location = (GridPoint) request.get("location");
        otherLocations.put(senderId, location);

        @SuppressWarnings("unchecked")
        ArrayDeque<GridPoint> requestingPoints = (ArrayDeque<GridPoint>) request.get("requestingPoints");
        otherRequestings.put(senderId, requestingPoints);

        @SuppressWarnings("unchecked")
        boolean state = (boolean) request.get("stay");
        otherState.put(senderId, state);

        @SuppressWarnings("unchecked")
        int noa = (int) request.get("numOfAvoid");
        otherNumOfAvoid.put(senderId, noa);

        @SuppressWarnings("unchecked")
        int senderLrd = (int) request.get("lrd");
        clk = Math.max(clk, senderLrd);

        HashMap<String, Object> content = new HashMap<>();
        content.put("topic", "reply");

        boolean priority; // for this node
        int accepted = 0;
        MyTopology tp = (MyTopology) getTopology();
        Priority judge = new Priority(P_LRD, tp.getPCs(), tp.getPRq());
        for (GridPoint point : requestingPoints) {
            priority = judge.getPriority(this, (AbstractGridNode) sender, msg, point);

            if (priority) {
                break;
            } else {
                accepted++;
            }
        }
        content.put("ok", accepted);
        send(sender, new Message(content));
    }

    /**
     * Called when receiving a message with topic "reply"
     * then update acceptedLocks, which means the number of points
     * that this node can be accepted to move, and a waiting node list
     */
    protected void receiveReply(Message msg) {
        Node sender = msg.getSender();

        /* for debugging */
        // GRID_LOG("received reply from " + sender.getID());
        /* end debugging */

        @SuppressWarnings("unchecked")
        HashMap<String, Object> reply = (HashMap<String, Object>) msg.getContent();

        acceptedLocks = Math.min(acceptedLocks, (int) reply.get("ok"));
        boolean removed = waitingFrom.remove(sender);

        /* for debugging */
        // if (removed)
        //     GRID_LOG("removed " + sender.getID());
        // else
        //     GRID_LOG("remove failed");
        /* end debugging */

    }

    public GridPoint getNext() {
        return next;
    }

    public GridPoint getPrev() {
        return prev;
    }

    public GridPoint getDestination() {
        return dest;
    }

    public int getLrd() {
        return lrd;
    }

    public HashMap<GridPoint, Integer> getNumOfReq() {
        return numOfReq;
    }

    public ArrayDeque<GridPoint> getPath() {
        return path;
    }

    public ArrayDeque<GridPoint> getLocking() {
        return locking;
        // return new ArrayDeque<GridPoint>(locking); // shallow copy
    }

    public ArrayDeque<GridPoint> getRequesting() {
        return requesting;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isStay() {
        return stay;
    }

    public boolean isAvoid() {
        return avoid;
    }

    public void setAvoid(boolean avoid) {
        this.avoid = avoid;
    }

    public int getNumOfAvoid() {
        return numOfAvoid;
    }

    public boolean isOnGridPoint() {
        double x = getX();
        double y = getY();
        // double xmod = x % (double)CELL_SIZE_X;
        // double ymod = y % (double)CELL_SIZE_Y;
        // GRID_LOG("isOnGridPoint: (xmod,ymod)=" + "(" + xmod + "," + ymod + ")");
        if (x % (double)CELL_SIZE_X == 0.0 && y % (double)CELL_SIZE_Y == 0.0)
            return true;
        else
            return false;
    }

    public int getNumTask() {
        return numTask;
    }

    public int getNumStay() {
        return numStay;
    }

    @Override
    public GridPoint getLocation() {
        return new GridPoint(getX(), getY());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractGridNode) {
            AbstractGridNode gn = (AbstractGridNode) obj;
            return this.getID() == gn.getID();
        }
        return super.equals(obj);
    }

}
