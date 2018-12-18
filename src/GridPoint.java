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

    public int calcManDist(GridPoint p2) {
        int coordX2 = p2.getCoordX();
        int coordY2 = p2.getCoordY();
        return Math.abs(getCoordX() - coordX2) + Math.abs(getCoordY() - coordY2);
    }

    @Override
    public String toString() {
        String str = "(" + getCoordX() + "," + getCoordY() + ")";
        return str;
    }
}
