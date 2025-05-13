package server.utility;

/**
 * Represents the different game types available on the RetroArcade multiplayer gaming platform.
 * <p>
 * This enum is used to differentiate between game-specific logic in areas such as
 * matchmaking, session management, and rating systems.
 */
public enum GameType {

    /**
     * Represents the game of Tic Tac Toe.
     */
    TicTacToe,

    /**
     * Represents the game of Connect Four.
     */
    ConnectFour,

    /**
     * Represents the game of Checkers.
     */
    Checkers
}

