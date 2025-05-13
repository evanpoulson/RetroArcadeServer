package server.utility;

/**
 * Represents a structured message exchanged between server threads in the multiplayer
 * gaming system.
 * <p>
 * Thread messages are used to route commands and data (e.g., connection requests, player moves,
 * game events) between different server components such as client handlers, matchmaking logic,
 * and game sessions.
 * <p>
 * Each message includes a {@link MessageType} that defines the nature of the message,
 * and a type-safe data payload of type {@code T}, which may contain game-specific data,
 * user actions, or other contextual information.
 *
 * @param <T> the type of the data payload attached to this message
 */
public class ThreadMessage<T> {

    /**
     * The type of the message, indicating its purpose or category.
     */
    private MessageType type;

    /**
     * The data payload associated with the message. May be {@code null} for control messages
     * that do not require additional data.
     */
    private T data;

    /**
     * Constructs a new {@code ThreadMessage} with the given type and associated data.
     *
     * @param type the type of message being sent
     * @param data the data payload, or {@code null} if no data is needed
     */
    public ThreadMessage(MessageType type, T data) {
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
    public T getData() {
        return data;
    }
}
