package server.utility;

/**
 * Represents a message passed between threads or components within the server.
 * <p>
 * Each message consists of a {@link MessageType} that identifies the purpose of the message,
 * and an optional data payload that can carry additional information.
 * This class facilitates structured and type-safe inter-thread communication.
 */
public class ThreadMessage {

    /**
     * The type of the message, indicating its purpose or category.
     */
    private MessageType type;

    /**
     * The data payload associated with the message. Can be {@code null} if no data is required.
     */
    private Object data;

    /**
     * Constructs a new {@code ThreadMessage} with the given type and associated data.
     *
     * @param type the type of message being sent
     * @param data the data payload, or {@code null} if no data is needed
     */
    public ThreadMessage(MessageType type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Constructs a new {@code ThreadMessage} with the given type and no data payload.
     *
     * @param type the type of message being sent
     */
    public ThreadMessage(MessageType type) {
        this.type = type;
        this.data = null;
    }

    /**
     * Returns the type of this message.
     *
     * @return the {@link MessageType} of the message
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Returns the data payload of this message.
     *
     * @return the message's data payload, or {@code null} if none was provided
     */
    public Object getData() {
        return data;
    }
}