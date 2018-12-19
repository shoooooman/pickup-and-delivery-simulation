import jbotsim.Topology;
import static constant.ConstEnvironment.*;
import java.awt.*;

public class GridPainter implements jbotsimx.ui.painting.BackgroundPainter {
    @Override
    public void paintBackground(Graphics2D g, Topology tp) {
        g.setColor(Color.gray);
        for (int i = 0; i < GRID_SIZE_X; i++) {
            for (int j = 0; j < GRID_SIZE_Y; j++) {
                g.drawLine(i*CELL_SIZE_X, 0, i*CELL_SIZE_X, CELL_SIZE_Y*GRID_SIZE_Y);
                g.drawLine(0, j*CELL_SIZE_Y, CELL_SIZE_X*GRID_SIZE_X, j*CELL_SIZE_Y);
            }
        }
    }

}
