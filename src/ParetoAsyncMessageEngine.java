import jbotsim.Node;
import jbotsim.Message;
import jbotsimx.messaging.AsyncMessageEngine;
import java.util.Random;
import static constant.ConstEnvironment.*;
import static constant.ConstExperiment.*;

public class ParetoAsyncMessageEngine extends AsyncMessageEngine {
    public ParetoAsyncMessageEngine(double averageDuration, Type type) {
        super(averageDuration, type);
    }

    @Override
    protected int drawDelay(Message m) {
        if (type == Type.FIFO){
            int max = 0;
            Node sender = m.getSender();
            Node destination = m.getDestination();
            for (Message m2 : delays.keySet())
                if (m2.getSender() == sender && m2.getDestination() == destination)
                    max = Math.max(max, delays.get(m2));

            return (int) (max + Math.round(Math.log(1 - Math.random()) / (-1.0/average)));
        } else {
            // generate rand based on pareto distribution
            double uniRand = Math.random();
            int rand = (int) (PARETO_X_M / Math.pow(1.0-uniRand, 1.0/PARETO_K));
            if (DEBUG)
                System.out.println("rand=" + rand);
            return rand;
        }
    }
}
