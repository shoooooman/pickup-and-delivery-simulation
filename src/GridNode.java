import jbotsim.Node;
import jbotsim.Point;
import jbotsim.Color;
import java.util.ArrayDeque;
import static constant.ConstEnvironment.*;

public class GridNode extends Node {
    static int id = 0;
    ArrayDeque<Point> path;
    Point next;
    boolean done = false;
    @Override
    public void onStart() {
        setID(id++);
        PathGenerator pathGen = new PathGenerator();
        path = pathGen.newPath(this.getLocation());
        next = path.remove();
        System.out.println("ID: " + getID() + " diffX=" + (next.getX()-this.getX()));
        System.out.println("ID: " + getID() + " diffY=" + (next.getY()-this.getY()));
        System.out.println("ID: " + getID() + " angle=" + Math.atan2(next.getY() - this.getY(), (next.getX() - this.getX())));
        setDirection(next);
        // setDirection(calcDirection(next));
    }
    @Override
    public void onClock() {
        this.setColor(Color.blue);
        Point here = this.getLocation();
        System.out.println("ID: " + getID() + " is " + here);
        if (here.equals(next)) {
            System.out.println("here: (" + here.getX()/CELL_SIZE_X + "," + here.getY()/CELL_SIZE_Y + ")");
            this.setColor(Color.red);
            if (path.isEmpty()) {
                System.out.println("ID: " + getID() + " complete!");
                done = true;

            } else {
                next = path.remove();
                System.out.println("ID: " + getID() + " diffX=" + (next.getX()-this.getX()));
                System.out.println("ID: " + getID() + " diffY=" + (next.getY()-this.getY()));
                System.out.println("ID: " + getID() + " angle=" + Math.atan2(next.getY() - this.getY(), (next.getX() - this.getX())));
                setDirection(next);
                // setDirection(calcDirection(next));
            }
        }
        if (!done)
            moveDir(1.0);
        // wrapLocation();
    }

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

}
