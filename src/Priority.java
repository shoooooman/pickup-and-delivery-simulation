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
        @SuppressWarnings("unchecked")
        int disCsS = ((GridPoint) requestContent.get("location")).calcManDist(requestPoint);

        int idR = receiver.getID();
        int idS = sender.getID();

        int norqR = receiver.getNumOfReq().getOrDefault(requestPoint, 0); // return 0 if the value is null
        @SuppressWarnings("unchecked")
        int norqS = ((HashMap<GridPoint, Integer>) requestContent.get("numOfReq")).getOrDefault(requestPoint, 0);

        if (receiver.getLocking().contains(requestPoint)) {
            return true;
        } else if (!receiver.getRequesting().contains(requestPoint)) {
            return false;
        } else {
            if        (pLrd && lrdR < lrdS) {
                System.out.println("lrd: ID " + receiver.getID() + "(" + lrdR + ")" + " < ID " + sender.getID() + "(" + lrdS + ")");
                return true;
            } else if (pLrd && lrdR > lrdS) {
                System.out.println("lrd: ID " + sender.getID() + "(" + lrdS + ")" + " < ID " + receiver.getID() + "(" + lrdR + ")");
                return false;
            } else {
                if        (pDisToCs && disCsR < disCsS) {
                    System.out.println("dis: ID " + receiver.getID() + "(" + disCsR + ")" + " < ID " + sender.getID() + "(" + disCsS + ")");
                    return true;
                } else if (pDisToCs && disCsR > disCsS) {
                    System.out.println("dis: ID " + sender.getID() + "(" + disCsS + ")" + " < ID " + receiver.getID() + "(" + disCsR + ")");
                    return false;
                } else {
                    if        (pNumOfReq && norqR > norqS) {
                        System.out.println("nor: ID " + receiver.getID() + "(" + norqR + ")" + " > ID " + sender.getID() + "(" + norqS + ")");
                        return true;
                    } else if (pNumOfReq && norqR < norqS) {
                        System.out.println("nor: ID " + sender.getID() + "(" + norqS + ")" + " > ID " + receiver.getID() + "(" + norqR + ")");
                        return false;
                    } else {
                        if (idR < idS) {
                            System.out.println("id: ID " + receiver.getID() +  " < ID " + sender.getID());
                            return true;
                        } else {
                            System.out.println("id: ID " + sender.getID() +  " < ID " + receiver.getID());
                            return false;
                        }
                    }
                }
            }
        }
    }
}
