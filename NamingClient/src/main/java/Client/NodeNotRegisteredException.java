package Client;

public class NodeNotRegisteredException extends Exception {
    public NodeNotRegisteredException(String message) {
        super(message);
    }

    public NodeNotRegisteredException(String message, Throwable err) {
        super(message, err);
    }
}
