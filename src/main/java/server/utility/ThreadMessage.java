package server.utility;

import server.player.PlayerHandler;
import server.session.GameSessionManager;

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
    private final MessageType type;

    /**
     * The data payload associated with the message. May be {@code null} for control messages
     * that do not require additional data.
     */
    private final T data;

    /**
     * The player who sent this message. May be null for system messages.
     */
    private final PlayerHandler playerSender;

    /**
     * The GameSessionManager that sent this message. May be null for player messages.
     */
    private final GameSessionManager gameSessionSender;

    /**
     * Constructs a new player-sent {@code ThreadMessage} with the given type, sender, and associated data.
     *
     * @param type the type of message being sent
     * @param sender the player who sent this message
     * @param data the data payload, or {@code null} if no data is needed
     */
    public ThreadMessage(MessageType type, PlayerHandler sender, T data) {
        this.type = type;
        this.playerSender = sender;
        this.gameSessionSender = null;
        this.data = data;
    }

    /**
     * Constructs a new GameSessionManager-sent {@code ThreadMessage} with the given type, sender, and associated data.
     *
     * @param type the type of message being sent
     * @param sender the GameSessionManager sending this message
     * @param data the data payload, or {@code null} if no data is needed
     */
    public ThreadMessage(MessageType type, GameSessionManager sender, T data) {
        this.type = type;
        this.playerSender = null;
        this.gameSessionSender = sender;
        this.data = data;
    }

    /**
     * Constructs a new system {@code ThreadMessage} with the given type and data payload.
     * Used for system messages that don't come from a specific component.
     *
     * @param type the type of message being sent
     * @param data the data payload, or {@code null} if no data is needed
     */
    public ThreadMessage(MessageType type, T data) {
        this.type = type;
        this.playerSender = null;
        this.gameSessionSender = null;
        this.data = data;
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

    /**
     * Returns the player who sent this message.
     *
     * @return the player sender of this message, or null if it's not a player message
     */
    public PlayerHandler getPlayerSender() {
        return playerSender;
    }

    /**
     * Returns the GameSessionManager that sent this message.
     *
     * @return the GameSessionManager sender of this message, or null if it's not a GameSessionManager message
     */
    public GameSessionManager getGameSessionSender() {
        return gameSessionSender;
    }

    /**
     * Checks if this message was sent by a player.
     *
     * @return true if the message was sent by a player, false otherwise
     */
    public boolean isFromPlayer() {
        return playerSender != null;
    }

    /**
     * Checks if this message was sent by a GameSessionManager.
     *
     * @return true if the message was sent by a GameSessionManager, false otherwise
     */
    public boolean isFromGameSession() {
        return gameSessionSender != null;
    }
}
