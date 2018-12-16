import jbotsim.Point;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Random;
import static constant.ConstEnvironment.*;

public class PathGenerator {
    ArrayDeque<Point> locations;

    public ArrayDeque<Point> newPath(Point cl) {
        Point dest = newDestination(cl);
        System.out.println("dest: (" + dest.getX()/CELL_SIZE_X+ "," + dest.getY()/CELL_SIZE_Y+ ")");
        HashMap<String, Integer> dist = getManDist(dest, cl);
        locations = new ArrayDeque<Point>();

        Point next = cl;
        while(true) {
            next = getNextLocation(next, dist);
            if (next == null) break;
            System.out.println("path: (" + next.getX()/CELL_SIZE_X+ "," + next.getY()/CELL_SIZE_Y+ ")");
            locations.add(next);
        }
        return locations;
    }

    private Point newDestination(Point start) {
        Random rand = new Random();
        Point dest = new Point(rand.nextInt(GRID_SIZE_X)*CELL_SIZE_X, rand.nextInt(GRID_SIZE_Y)*CELL_SIZE_Y);
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

    private Point getNextLocation(Point location, HashMap<String, Integer> dist) {
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
            return new Point(lx, ly-diff*CELL_SIZE_Y);
        } else if (dy == 0) {
            int diff = dx>0 ? -1 : 1;
            dx = dx + diff;
            dist.put("x", dx);
            return new Point(lx-diff*CELL_SIZE_X, ly);
        } else {
            double rn = Math.random()*2;
            if (rn < 1.0) {
                int diff = dy>0 ? -1 : 1;
                dy = dy + diff;
                dist.put("y", dy);
                return new Point(lx, ly-diff*CELL_SIZE_Y);
            } else {
                int diff = dx>0 ? -1 : 1;
                dx = dx + diff;
                dist.put("x", dx);
                return new Point(lx-diff*CELL_SIZE_X, ly);
            }

        }
    }
}
