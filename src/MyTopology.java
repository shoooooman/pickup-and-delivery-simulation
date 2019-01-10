import jbotsim.Topology;
import jbotsim.Point;
import jbotsim.Node;
import java.util.ArrayList;
import java.util.Random;
import static constant.ConstEnvironment.*;
import static constant.ConstUser.*;
import static constant.ConstExperiment.*;

public class MyTopology extends Topology {
    // for experiment
    ExcelWriter writer = new ExcelWriter();
    int dataNo = 0;     // count total number of data
    int runCounter = 0; // count the number of trials with the same condition
    int dindex = 0;     // indexes of arrays in ConstEnvironment
    int windex = 0;
    int nindex = 0;
    // end experiment

    /**
     * Deploy nodes randomly although the initial positions are not the same.
     */
    public void deployNodes() {
        Random rand = new Random();
        ArrayList<Point> initPoint = new ArrayList<>();
        for (int i = 0; i < getNodeNum(); i++) {
            // avoid the same initial point
            double x, y;
            do {
                x = rand.nextInt(GRID_SIZE_X)*CELL_SIZE_X;
                y = rand.nextInt(GRID_SIZE_Y)*CELL_SIZE_Y;
            } while (initPoint.contains(new Point(x, y)));
            initPoint.add(new Point(x, y));
            switch(NODE_TYPE) {
                case Window:
                    addNode(x, y, new GridNode());
                    break;
                case Simple:
                    addNode(x, y, new SimpleGridNode());
                    break;
                case Ghost:
                    addNode(x, y, new GhostNode());
                    break;
            }
        }
    }

    /**
     * Initialize topology and time
     * "onStart()" is not called explicitly in this func
     * and that is the defference with "restart()"
     */
    public void nextTrial() {
        pause();
        clear();
        resetTime();
        deployNodes(); // called onStart() in it
        resume();
    }

    /**
     * Return a list of the number of stays for each node
     */
    public ArrayList<Integer> getNumStays() {
        ArrayList<Integer> list = new ArrayList<>();
        ArrayList<Node> nodes = (ArrayList<Node>) getNodes();
        for (Node node : nodes) {
            AbstractGridNode gnode = (AbstractGridNode) node;
            list.add(gnode.getID(), gnode.getNumStay());
        }
        return list;
    }

    /**
     * Return a list of the number of completed tasks for each node
     */
    public ArrayList<Integer> getNumTasks() {
        ArrayList<Integer> list = new ArrayList<>();
        ArrayList<Node> nodes = (ArrayList<Node>) getNodes();
        for (Node node : nodes) {
            AbstractGridNode gnode = (AbstractGridNode) node;
            list.add(gnode.getID(), gnode.getNumTask());
        }
        return list;
    }

    public ExcelWriter getExcelWriter() {
        return writer;
    }

    public int getDataNo() {
        return dataNo;
    }

    public int incDataNo() {
        return ++dataNo;
    }

    public int getRunCounter() {
        return runCounter;
    }

    public void setRunCounter(int count) {
        runCounter = count;
    }

    public int incRunCounter() {
        return ++runCounter;
    }

    public int getDIndex() {
        return dindex;
    }

    public int incDIndex() {
        return ++dindex;
    }

    public int getWIndex() {
        return windex;
    }

    public void setWIndex(int index) {
        windex = index;
    }

    public int incWIndex() {
        return ++windex;
    }

    public int getNIndex() {
        return nindex;
    }

    public void setNIndex(int index) {
        nindex = index;
    }

    public int incNIndex() {
        return ++nindex;
    }

    public int getWindowSize() {
        return EXPERIMENT ? WINDOW_SIZES[windex] : MAX_LOCKS;
    }

    public int getDelay() {
        return EXPERIMENT ? DELAY_AVERAGES[dindex] : DELAY_AVERAGE;
    }

    public int getNodeNum() {
        return EXPERIMENT ? NODE_NUMS[nindex] : NODE_NUM;
    }
}
