package constant;

public class ConstUser {
    private ConstUser() {}
    // Window Size
    public final static int MAX_LOCKS = 3;

    // The Number of Nodes
    public final static int NODE_NUM = 10;

    // Node Type
    public enum NodeType {
        Window,
        Simple,
        Ghost
    };
    public final static NodeType NODE_TYPE = NodeType.Window;

    // Priority
    public final static boolean P_LRD = true;
    public final static boolean P_CS = true;
    public final static boolean P_RQ = true;
}
