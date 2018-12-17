import jbotsim.Point;
import static constant.ConstEnvironment.*;

public class GridPoint extends Point {
    GridPoint() {
    }

    GridPoint(Point p) {
        super(p);
    }

    GridPoint(double x, double y) {
        super(x, y);
    }

    GridPoint(double x, double y, double z) {
        super(x, y, z);
    }

    public int getCoordX() {
        return (int) getX()/CELL_SIZE_X;
    }

    public int getCoordY() {
        return (int) getY()/CELL_SIZE_Y;
    }

    @Override
    public String toString() {
        String str = "(" + getCoordX() + "," + getCoordY() + ")";
        return str;
    }
}
