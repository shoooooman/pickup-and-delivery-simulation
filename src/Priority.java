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

    public boolean getPriority(GridNode receiver, GridNode sender, Message request, GridPoint dest) {
        assert(sender == request.getSender());

        @SuppressWarnings("unchecked")
        HashMap<String, Object> requestContent = (HashMap<String, Object>) request.getContent();

        int lrdR = receiver.getLrd();
        int lrdS = (int) requestContent.get("lrd");

        int disCsR = receiver.getLocation().calcManDist(dest);
        int disCsS = sender.getLocation().calcManDist(dest);

        int idR = receiver.getID();
        int idS = sender.getID();

        int norqR = receiver.getNumOfReq().getOrDefault(dest, 0); // return 0 if the value is null
        int norqS = sender.getNumOfReq().getOrDefault(dest, 0);

        GridPoint currentCoords = receiver.getLocation();
        GridPoint previousCoords = receiver.getPrev();

        if        (!receiver.isDone() && currentCoords.equals(dest)) {
            return true;
        } else if (!receiver.isDone() && previousCoords.equals(dest)) {
            return true;
        } else if (receiver.getLocking().contains(dest)) {
            return true;
        } else if (!receiver.getRequesting().contains(dest)) {
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
