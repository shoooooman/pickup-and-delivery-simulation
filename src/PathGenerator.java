import jbotsim.Point;
import jbotsim.Node;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Random;
import static constant.ConstEnvironment.*;
import static constant.ConstExperiment.*;

public class PathGenerator {
    ArrayDeque<GridPoint> points;

    /**
     * The first argument is for debugging
     */
    public ArrayDeque<GridPoint> newPath(int id, Point cl, Point dest) {
        if (DEBUG)
            System.out.println("ID: " + id + " dest: " + dest);
        HashMap<String, Integer> dist = getManDist(dest, cl);
        points = new ArrayDeque<GridPoint>();

        GridPoint next = new GridPoint(cl.getX(), cl.getY());
        if (DEBUG)
            System.out.println("ID: " + id + " init: " + next);
        while(true) {
            next = getNextLocation(next, dist);
            if (next == null) break;
            if (DEBUG)
                System.out.println("ID: " + id + " path: " + next);
            points.add(next);
        }
        return points;
    }

    public ArrayDeque<GridPoint> newPath(int id, Point cl, ArrayDeque<GridPoint> part, Point dest) {
        if (DEBUG)
            System.out.println("ID: " + id + " dest: " + dest);
        HashMap<String, Integer> dist = getManDist(dest, cl);
        points = new ArrayDeque<GridPoint>();

        while (!part.isEmpty()) {
            points.add(part.remove());
        }

        GridPoint next;
        if (!points.isEmpty()) {
            next = points.getLast();
        } else {
            next = new GridPoint(cl.getX(), cl.getY());
        }
        while(true) {
            next = getNextLocation(next, dist);
            if (next == null) break;
            if (DEBUG)
                System.out.println("ID: " + id + " path: " + next);
            points.add(next);
        }
        return points;

    }

    public ArrayDeque<GridPoint> newPath(int id, Point cl) {
        GridPoint dest = newDestination(cl);
        return newPath(id, cl, dest);
    }

    public GridPoint newDestination(Point start) {
        Random rand = new Random();
        GridPoint dest = new GridPoint(rand.nextInt(GRID_SIZE_X+1)*CELL_SIZE_X, rand.nextInt(GRID_SIZE_Y+1)*CELL_SIZE_Y);
        return dest;
    }

    public HashMap<String, Integer> getManDist(Point p1, Point p2) {
        final double x1 = p1.getX();
        final double y1 = p1.getY();
        final double x2 = p2.getX();
        final double y2 = p2.getY();
        HashMap<String, Integer> dist = new HashMap<>();
        final int dx = (int)((x1-x2) / CELL_SIZE_X);
        final int dy = (int)((y1-y2) / CELL_SIZE_Y);
        dist.put("x", dx);
        dist.put("y", dy);
        return dist;
    }

    private GridPoint getNextLocation(GridPoint location, HashMap<String, Integer> dist) {
        final double lx = location.getX();
        final double ly = location.getY();
        int dx = dist.get("x");
        int dy = dist.get("y");
        if (dx == 0 && dy == 0) {
            return null;
        } else if (dx == 0) {
            int diff = dy>0 ? -1 : 1;
            dy = dy + diff;
            dist.put("y", dy);
            return new GridPoint(lx, ly-diff*CELL_SIZE_Y);
        } else if (dy == 0) {
            int diff = dx>0 ? -1 : 1;
            dx = dx + diff;
            dist.put("x", dx);
            return new GridPoint(lx-diff*CELL_SIZE_X, ly);
        } else {
            double rn = Math.random()*2;
            if (rn < 1.0) {
                int diff = dy>0 ? -1 : 1;
                dy = dy + diff;
                dist.put("y", dy);
                return new GridPoint(lx, ly-diff*CELL_SIZE_Y);
            } else {
                int diff = dx>0 ? -1 : 1;
                dx = dx + diff;
                dist.put("x", dx);
                return new GridPoint(lx-diff*CELL_SIZE_X, ly);
            }

        }
    }
}
