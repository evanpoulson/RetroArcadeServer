package server.utility;

/**
 * Represents the types of messages that can be sent between threads or components
 * in the server system.
 * <p>
 * This enum is typically used with messaging mechanisms to categorize the intent
 * or purpose of a message.
 */
public enum MessageType {

    // System messages
    /**
     * Indicates that a connection has been established or requested.
     */
    CONNECT,

    /**
     * Indicates that a disconnection has occurred or has been requested.
     */
    DISCONNECT,

    /**
     * Represents an error state or conveys information about a failure.
     */
    ERROR,

    // Game control messages
    /**
     * Request to pause the game. Can only be sent by the current player.
     */
    PAUSE_REQUEST,

    /**
     * Request to resume the game. Can only be sent by the player who paused it.
     */
    RESUME_REQUEST,

    /**
     * Notification that the game has been paused.
     */
    GAME_PAUSED,

    /**
     * Notification that the game has been resumed.
     */
    GAME_RESUMED,

    // Turn management
    /**
     * Notification that it is now a player's turn.
     */
    YOUR_TURN,

    /**
     * Notification that it is another player's turn.
     */
    OTHER_PLAYER_TURN,

    /**
     * Notification that a move was ignored because it wasn't the player's turn.
     */
    NOT_YOUR_TURN,

    // Game state updates
    /**
     * A player has made a move.
     */
    MOVE_MADE,

    /**
     * The game has ended with a winner.
     */
    GAME_WON,

    /**
     * The game has ended in a draw.
     */
    GAME_DRAWN,

    /**
     * The current state of the game board.
     */
    GAME_STATE_UPDATE
}

