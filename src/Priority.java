import jbotsim.Message;
import java.util.HashMap;

public class Priority {
    private boolean pLrd ;
    private boolean pDisToCs;
    private boolean pNumOfReq;

    Priority() {
        this.pLrd      = true;
        this.pDisToCs  = true;
        this.pNumOfReq = true;
    }

    Priority(boolean pLrd, boolean pDisToCs, boolean pNumOfReq) {
        this.pLrd      = pLrd;
        this.pDisToCs  = pDisToCs;
        this.pNumOfReq = pNumOfReq;
    }

    public boolean getPriority(AbstractGridNode receiver, AbstractGridNode sender, Message request, GridPoint requestPoint) {
        assert(sender == request.getSender());

        @SuppressWarnings("unchecked")
        HashMap<String, Object> requestContent = (HashMap<String, Object>) request.getContent();

        int lrdR = receiver.getLrd();
        int lrdS = (int) requestContent.get("lrd");

        int disCsR = receiver.getLocation().calcManDist(requestPoint);
        int disCsS = sender.getLocation().calcManDist(requestPoint);

        int idR = receiver.getID();
        int idS = sender.getID();

        int norqR = receiver.getNumOfReq().getOrDefault(requestPoint, 0); // return 0 if the value is null
        int norqS = sender.getNumOfReq().getOrDefault(requestPoint, 0);

        GridPoint currentCoords = receiver.getLocation();
        GridPoint previousCoords = receiver.getPrev();

        if        (currentCoords.equals(requestPoint)) {
            return true;
        } else if (receiver.getLocking().contains(requestPoint)) {
            return true;
        } else if (!receiver.getRequesting().contains(requestPoint)) {
            return false;
        } else {
            if        (pLrd && lrdR < lrdS) {
                return true;
            } else if (pLrd && lrdR > lrdS) {
                return false;
            } else {
                if        (pDisToCs && disCsR < disCsS) {
                    return true;
                } else if (pDisToCs && disCsR > disCsS) {
                    return false;
                } else {
                    if        (pNumOfReq && norqR > norqS) {
                        return true;
                    } else if (pNumOfReq && norqR < norqS) {
                        return false;
                    } else {
                        if (idR < idS) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
    }
}