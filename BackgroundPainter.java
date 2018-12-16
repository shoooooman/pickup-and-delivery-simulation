import jbotsim.Topology;

import java.awt.*;

public class BackgroundPainter implements jbotsimx.ui.painting.BackgroundPainter {
    @Override
    public void paintBackground(Graphics2D g, Topology tp) {
        g.setColor(Color.gray);
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 8; j++) {
                g.drawLine(i*50, 0, i*50, 400);
                g.drawLine(0, j*50, 600, j*50);
            }
        }
    }

}
