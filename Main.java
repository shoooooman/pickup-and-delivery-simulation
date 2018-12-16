import jbotsim.Topology;
import jbotsimx.ui.JViewer;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Topology tp = new Topology();
        JViewer jv = new JViewer(tp);
        jv.getJTopology().addBackgroundPainter(new BackgroundPainter());
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            tp.addNode(rand.nextInt(12)*50, rand.nextInt(8)*50, new GridNode());
        }
        tp.setClockSpeed(100);
        tp.start();
    }
}
