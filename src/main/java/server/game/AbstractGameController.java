package server.game;

import server.player.PlayerHandler;
import server.utility.ThreadMessage;

import java.util.Set;

/**
 * Abstract base class that implements common functionality for all game controllers.
 * Provides default implementations for some methods and enforces common game rules.
 * 
 * This class handles:
 * 1. Basic game state management (running, paused, over)
 * 2. Player turn tracking
 * 3. Winner tracking
 * 4. Common validation rules
 * 
 * Subclasses should:
 * 1. Implement game-specific move validation and processing
 * 2. Define game-specific win conditions
 * 3. Manage game-specific state
 * 4. Override methods as needed for game-specific behavior
 */
public abstract class AbstractGameController implements GameController {
    /** The set of players participating in the game */
    protected final Set<PlayerHandler> players;
    
    /** The player whose turn it is to make a move */
    protected PlayerHandler currentPlayer;
    
    /** Flag indicating if the game has reached a terminal state */
    protected boolean gameOver;
    
    /** The player who won the game, if any */
    protected PlayerHandler winner;
    
    /** Flag indicating if the game is currently paused */
    protected boolean isPaused;

    /**
     * Constructs a new game controller with the given set of players.
     * 
     * @param players The set of players participating in the game
     * @throws IllegalArgumentException if players is null or empty
     */
    protected AbstractGameController(Set<PlayerHandler> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players set cannot be null or empty");
        }
        this.players = players;
        this.gameOver = false;
        this.winner = null;
        this.isPaused = false;
    }

    /**
     * Initializes the game state for a new session.
     * Sets the first player in the set as the current player
     * and resets all game state flags.
     */
    @Override
    public void initializeGame() {
        // Default implementation: first player in the set goes first
        currentPlayer = players.iterator().next();
        gameOver = false;
        winner = null;
        isPaused = false;
    }

    /**
     * Checks if the game has reached a terminal state.
     * 
     * @return true if the game is over, false if it should continue
     */
    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Gets the player who should make the next move.
     * 
     * @return The PlayerHandler for the player whose turn it is
     */
    @Override
    public PlayerHandler getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Gets the winner of the game, if the game is over.
     * 
     * @return The PlayerHandler of the winner, or null if the game is not over or ended in a draw
     */
    @Override
    public PlayerHandler getWinner() {
        return winner;
    }

    /**
     * Resets the game state to start a new game with the same players.
     * Calls initializeGame() to reset all game state.
     */
    @Override
    public void resetGame() {
        initializeGame();
    }

    /**
     * Pauses the game, saving the current state.
     * Sets the isPaused flag to true.
     */
    @Override
    public void pauseGame() {
        isPaused = true;
    }

    /**
     * Resumes the game from a paused state.
     * Sets the isPaused flag to false.
     */
    @Override
    public void resumeGame() {
        isPaused = false;
    }

    /**
     * Switches the current player to the other player.
     * This method assumes there are exactly two players in the game.
     */
    protected void switchPlayer() {
        for (PlayerHandler player : players) {
            if (player != currentPlayer) {
                currentPlayer = player;
                break;
            }
        }
    }

    /**
     * Sets the game as over and records the winner.
     * 
     * @param winner The winning player, or null for a draw
     */
    protected void endGame(PlayerHandler winner) {
        this.gameOver = true;
        this.winner = winner;
    }

    /**
     * Validates that the given player is allowed to make a move.
     * A player can move if:
     * 1. The game is not over
     * 2. The game is not paused
     * 3. It is the player's turn
     * 
     * @param player The player attempting to make a move
     * @return true if the player can move, false otherwise
     */
    protected boolean validatePlayerTurn(PlayerHandler player) {
        return !gameOver && !isPaused && player == currentPlayer;
    }
} 