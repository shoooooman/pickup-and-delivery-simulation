package constant;

public class ConstUser {
    private ConstUser() {}
    public final static int MAX_LOCKS = 3;
    public final static int NODE_NUM = 10;
    public enum NodeType {
        Window,
        Simple,
        Ghost
    };
    public final static NodeType NODE_TYPE = NodeType.Window;
}
