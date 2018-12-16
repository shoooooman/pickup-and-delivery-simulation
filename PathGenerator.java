import jbotsim.Point;
import java.util.ArrayDeque;

public class PathGenerator {
    final int PATH_LEN = 15;
    ArrayDeque<Point> locations;

    public ArrayDeque<Point> newPath(Point cl) {
        locations = new ArrayDeque<Point>();

        Point next = cl;
        for (int i = 0; i < PATH_LEN; i++) {
            next = getNextLocation(next);
            System.out.println("i=" + i + ": (" + next.getX() + "," + next.getY() + ")");
            locations.add(next);
        }
        return locations;
    }

    private Point getNextLocation(Point location) {
        final double lx = location.getX();
        final double ly = location.getY();
        Point next = new Point();
        double rn = Math.random()*4;
        if      (rn < 1.0) {
            next.setLocation(lx-50, ly);
            return next;
        }
        else if (rn < 2.0) {
            next.setLocation(lx+50, ly);
            return next;
        }
        else if (rn < 3.0) {
            next.setLocation(lx, ly-50);
            return next;
        } else {
            next.setLocation(lx, ly+50);
            return next;
        }
    }
}
