import jbotsim.Node;
import jbotsim.Point;
import jbotsim.Color;
import java.util.ArrayDeque;

public class GridNode extends Node {
    static int id = 0;
    ArrayDeque<Point> path;
    Point next;
    @Override
    public void onStart() {
        setID(id++);
        PathGenerator pathGen = new PathGenerator();
        path = pathGen.newPath(this.getLocation());
        next = path.remove();
        setDirection(next);
    }
    @Override
    public void onClock() {
        this.setColor(Color.blue);
        Point here = this.getLocation();
        System.out.println("ID: " + getID() + " is " + here);
        if (here.equals(next)) {
            this.setColor(Color.red);
            next = path.remove();
            setDirection(next);
        }
        System.out.println("moving to " + next);
        move(5.0);
        wrapLocation();
    }

    private double randomDirection() {
        double rn = Math.random()*4;
        if      (rn < 1.0) return 0.0*Math.PI;
        else if (rn < 2.0) return 0.5*Math.PI;
        else if (rn < 3.0) return 1.0*Math.PI;
        else               return 1.5*Math.PI;
    }

}
