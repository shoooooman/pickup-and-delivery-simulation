import jbotsim.Node;
import jbotsim.Point;
import jbotsim.Color;
import jbotsim.Message;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.ArrayList;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;

public abstract class AbstractGridNode extends Node {
    static int id = 0;
    protected ArrayDeque<GridPoint> path;
    protected GridPoint next, prev, dest;
    protected boolean done = false;
    protected boolean stay = false;
    protected boolean waiting = false;
    protected ArrayDeque<GridPoint> requesting = new ArrayDeque<>();
    protected ArrayDeque<GridPoint> locking = new ArrayDeque<>();
    protected int acceptedLocks = MAX_LOCKS;
    protected ArrayList<Node> waitingFrom = new ArrayList<>();
    protected int lrd = 0; // last request date
    protected int clk = 0; // logical clock
    protected HashMap<GridPoint, Integer> numOfReq = new HashMap<>();
    protected boolean pLrd = true;
    protected boolean pDisToCs = true;
    protected boolean pNumOfReq = true;

    public AbstractGridNode() {
        setIcon("/icon/node.png");
        setSize(10);
    }

    @Override
    public void onStart() {
        setID(id++);
        PathGenerator pathGen = new PathGenerator();
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

    @Override
    public void onPostClock() {
        if (!done && !stay)
            moveDir(1.0);
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
     * Update a requesting point list with path
     * then send a request message to all the other nodes
     */
    protected void sendRequest() {
        while (requesting.size() + locking.size() < MAX_LOCKS) {
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

        HashMap<String, Object> content = new HashMap<>();
        content.put("topic", "request");
        content.put("locations", requesting.clone());
        content.put("lrd", lrd);
        content.put("numOfReq", numOfReq);
        Object obj = (Object) content;
        Message msg = new Message(content);
        GRID_LOG("sending request");
        sendAll(msg);
    }

    /**
     * Called when receiving a message with topic "request"
     * then caliculates the priorities for all requesting points
     * send back a maximum number which this node can accept to be locked from sender
     */
    protected void receiveRequest(Message msg) {
        Node sender = msg.getSender();

        /* for debugging */
        GRID_LOG("received request from " + sender.getID());
        /* end debugging */

        @SuppressWarnings("unchecked")
        HashMap<String, Object> request = (HashMap<String, Object>) msg.getContent();
        @SuppressWarnings("unchecked")
        ArrayDeque<GridPoint> locations = (ArrayDeque<GridPoint>) request.get("locations");
        @SuppressWarnings("unchecked")
        int senderLrd = (int) request.get("lrd");
        clk = Math.max(clk, senderLrd);

        HashMap<String, Object> content = new HashMap<>();
        content.put("topic", "reply");

        boolean priority; // for this node
        int accepted = 0;
        Priority judge = new Priority(pLrd, pDisToCs, pNumOfReq);
        for (GridPoint location : locations) {
            priority = judge.getPriority(this, (AbstractGridNode) sender, msg, location);

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
        GRID_LOG("received reply from " + sender.getID());
        /* end debugging */

        @SuppressWarnings("unchecked")
        HashMap<String, Object> reply = (HashMap<String, Object>) msg.getContent();
        acceptedLocks = Math.min(acceptedLocks, (int)reply.get("ok"));
        boolean removed = waitingFrom.remove(sender);

        /* for debugging */
        if (removed)
            GRID_LOG("removed " + sender.getID());
        else
            GRID_LOG("remove failed");
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

    public ArrayDeque<GridPoint> getLocking() {
        return locking;
        // return new ArrayDeque<GridPoint>(locking); // shallow copy
    }

    public ArrayDeque<GridPoint> getRequesting() {
        return requesting;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public boolean isStay() {
        return stay;
    }

    public void setPLrd(boolean priority) {
        pLrd = priority;
    }

    public void setPDisToCs(boolean priority) {
        pDisToCs = priority;
    }

    public void setPNumOfReq(boolean priority) {
        pNumOfReq = priority;
    }

    public boolean isOnGridPoint() {
        double x = getX();
        double y = getY();
        if (x % CELL_SIZE_X == 0 && y % CELL_SIZE_Y == 0)
            return true;
        else
            return false;
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
