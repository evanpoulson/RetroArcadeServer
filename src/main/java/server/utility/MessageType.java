package server.utility;

/**
 * Represents the types of messages that can be sent between threads or components
 * in the server system.
 * <p>
 * This enum is typically used with messaging mechanisms to categorize the intent
 * or purpose of a message.
 * <p>
 * Each message type has specific data requirements:
 * <ul>
 *   <li>Messages with no data should use {@code ThreadMessage<Void>}</li>
 *   <li>Messages with simple data should use appropriate primitive or object types</li>
 *   <li>Messages with complex data should use appropriate data structures</li>
 * </ul>
 */
public enum MessageType {

    // System messages
    /**
     * Indicates that a connection has been established or requested.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    CONNECT,

    /**
     * Indicates that a disconnection has occurred or has been requested.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    DISCONNECT,

    /**
     * Represents an error state or conveys information about a failure.
     * Data: {@code ThreadMessage<String>} - Error message string
     */
    ERROR,

    // Game control messages
    /**
     * Request to pause the game. Can only be sent by the current player.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    PAUSE_REQUEST,

    /**
     * Request to resume the game. Can only be sent by the player who paused it.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    RESUME_REQUEST,

    /**
     * Notification that the game has been paused.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    GAME_PAUSED,

    /**
     * Notification that the game has been resumed.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    GAME_RESUMED,

    // Turn management
    /**
     * Notification that it is now a player's turn.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    YOUR_TURN,

    /**
     * Notification that it is another player's turn.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    OTHER_PLAYER_TURN,

    /**
     * Notification that a move was ignored because it wasn't the player's turn.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    NOT_YOUR_TURN,

    // Game state updates
    /**
     * A player has made a move.
     * Data: {@code ThreadMessage<int[]>} - Array of [row, col] coordinates for the move
     */
    MOVE_MADE,

    /**
     * The game has ended with a winner.
     * Data: {@code ThreadMessage<PlayerHandler>} - The winning player
     */
    GAME_WON,

    /**
     * The game has ended in a draw.
     * Data: {@code ThreadMessage<Void>} - No data required
     */
    GAME_DRAWN,

    /**
     * The current state of the game board.
     * Data: {@code ThreadMessage<Map<String, Object>>} - Map containing:
     * <ul>
     *   <li>"gameState": The current game board state (type depends on game)</li>
     *   <li>"playerPiece": The player's assigned piece (e.g., 'X' or 'O' for TicTacToe)</li>
     * </ul>
     */
    GAME_STATE_UPDATE
}

