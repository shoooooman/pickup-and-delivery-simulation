import jbotsim.Node;
import jbotsim.Point;
import jbotsim.Color;
import jbotsim.Message;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.ArrayList;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;

public class GridNode extends Node {
    static int id = 0;
    ArrayDeque<GridPoint> path;
    GridPoint next, prev, dest;
    boolean done = false;
    boolean stay = false;
    boolean waiting = false;
    ArrayDeque<GridPoint> requesting = new ArrayDeque<>();
    ArrayDeque<GridPoint> locking = new ArrayDeque<>();
    int acceptedLocks = MAX_LOCKS;
    ArrayList<Node> waitingFrom = new ArrayList<>();

    public GridNode() {
        setIcon("/fig/node.png");
        setSize(10);
    }

    @Override
    public void onStart() {
        setID(id++);
        PathGenerator pathGen = new PathGenerator();
        path = pathGen.newPath(this, this.getLocation());
        // when init point is the same as dest, path will be empty
        if (!path.isEmpty()) {
            dest = path.getLast();
        } else {
            dest = getLocation();
        }
        next = this.getLocation();
    }
    @Override
    public void onClock() {
        this.setColor(Color.blue);
        GridPoint here = this.getLocation();
        if (here.equals(next)) {
            // GRID_LOG("(" + here.getX()/CELL_SIZE_X + "," + here.getY()/CELL_SIZE_Y + ")");
            GRID_LOG(here);
            GRID_LOG("locking ", false);
            for (GridPoint p : locking) {
                // System.out.print("(" + p.getX()/CELL_SIZE_X + "," + p.getY()/CELL_SIZE_Y + ") ");
                System.out.print(p);
            }
            System.out.println();

            boolean released = locking.remove(next);
            if (released)
                GRID_LOG("released " + prev);

            if (done)
                this.setColor(Color.green);
            else
                this.setColor(Color.red);

            if (!done && !waiting) {
                acceptedLocks = MAX_LOCKS;
                sendRequest();
                waitingFrom = (ArrayList<Node>)this.getNeighbors();
                waiting = true;
                stay = true;
            }
            if (waiting) {
                // when got replies from all the other nodes
                if (waitingFrom.isEmpty()) {
                    System.out.println("ID: " + getID() + " acceptedLocks=" + acceptedLocks);
                    for (int i = 0; i < acceptedLocks; i++) {
                        if (requesting.isEmpty()) break;
                        locking.add(requesting.remove());
                    }
                    waiting = false;
                    stay = false;
                } else {
                    GRID_LOG("waitingFrom ", false);
                    for (Node node : waitingFrom)
                        System.out.print(node.getID() + " ");
                    System.out.println();
                    stay = true;
                    // set direction this node is heading to
                    if (!requesting.isEmpty())
                        setDirection(requesting.element());
                }
            }
            if (!done && !waiting) {
                if (here.equals(dest)) {
                    GRID_LOG("complete!");
                    // when completing the task, the node is regarded as absence
                    locking.remove(next);
                    done = true;
                } else if (!locking.isEmpty()) {
                    prev = next;
                    next = locking.element();
                    stay = false;
                    setDirection(next);
                } else {
                    // when can get no locks
                    GRID_LOG("staying");
                    stay = true;
                }
            }
        }
    }
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

    private void GRID_LOG(Object log) {
        System.out.println("ID: " + getID() + " " + log);
    }
    private void GRID_LOG(Object log, boolean newLine) {
        if (newLine)
            System.out.println("ID: " + getID() + " " + log);
        else
            System.out.print  ("ID: " + getID() + " " + log);
    }

    /**
     * Return exact direction from a current point to the argument point
     */
    private double calcDirection(Point p) {
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
    private void sendRequest() {
        while (requesting.size() < MAX_LOCKS) {
            if (path.isEmpty()) break;
            requesting.add(path.remove());
        }
        HashMap<String, Object> content = new HashMap<>();
        content.put("topic", "request");
        content.put("locations", requesting.clone());
        Object obj = (Object)content;
        Message msg = new Message(content);
        GRID_LOG("sending request");
        sendAll(msg);
    }

    /**
     * Called when receiving a message with topic "request"
     * then caliculates the priorities for all requesting points
     * send back a maximum number which this node can accept to be locked from sender
     */
    private void receiveRequest(Message msg) {
        Node sender = msg.getSender();
        GRID_LOG("received request from " + sender.getID());
        @SuppressWarnings("unchecked")
        HashMap<String, Object> request = (HashMap<String, Object>) msg.getContent();
        @SuppressWarnings("unchecked")
        ArrayDeque<GridPoint> locations = (ArrayDeque<GridPoint>) request.get("locations");
        HashMap<String, Object> content = new HashMap<>();
        content.put("topic", "reply");

        boolean priority; // for this node
        int accepted = 0;
        for (GridPoint location : locations) {
            if (!done && location.equals(this.getLocation())) {
                priority = true;
            } else if (locking.contains(location)) {
                priority = true;
            } else if (!requesting.contains(location)) {
                priority = false;
            } else {
                if (this.getID() < sender.getID())
                    priority = true;
                // double rand = Math.random()*2;
                // if (rand < 1.0)
                //     priority = true;
                else
                    priority = false;
            }

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
    private void receiveReply(Message msg) {
        Node sender = msg.getSender();
        GRID_LOG("received reply from " + sender.getID());
        @SuppressWarnings("unchecked")
        HashMap<String, Object> reply = (HashMap<String, Object>) msg.getContent();
        acceptedLocks = Math.min(acceptedLocks, (int)reply.get("ok"));
        boolean removed = waitingFrom.remove(sender);
        if (removed)
            GRID_LOG("removed " + sender.getID());
        else
            GRID_LOG("remove failed");
    }

    @Override
    public GridPoint getLocation() {
        return new GridPoint(getX(), getY());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GridNode) {
            GridNode gn = (GridNode) obj;
            return this.getID() == gn.getID();
        }
        return super.equals(obj);
    }

}
