package server.game;

import server.player.PlayerHandler;
import server.utility.ThreadMessage;

/**
 * Interface defining the common operations required for any game type in the RetroArcade platform.
 * This allows the GameSessionManager to handle different game types uniformly.
 * 
 * Game controllers are responsible for:
 * 1. Managing game state and rules
 * 2. Processing player moves
 * 3. Determining game outcomes
 * 4. Handling game-specific messages
 */
public interface GameController {
    
    /**
     * Initializes the game state for a new session.
     * Called when a new game session is created.
     */
    void initializeGame();
    
    /**
     * Processes a move made by a player.
     * 
     * @param player The player making the move
     * @param moveData The data representing the move (type depends on game implementation)
     * @return true if the move was valid and processed successfully, false otherwise
     */
    boolean processMove(PlayerHandler player, Object moveData);
    
    /**
     * Checks if the game has reached a terminal state (win, loss, or draw).
     * 
     * @return true if the game is over, false if it should continue
     */
    boolean isGameOver();
    
    /**
     * Gets the current state of the game board.
     * The format of the state depends on the specific game implementation.
     * 
     * @return Object representing the current game state
     */
    Object getGameState();
    
    /**
     * Gets the player who should make the next move.
     * 
     * @return The PlayerHandler for the player whose turn it is
     */
    PlayerHandler getCurrentPlayer();
    
    /**
     * Handles a message received from a player.
     * This could be moves, chat messages, or other game-specific commands.
     * 
     * @param message The message to process
     * @return true if the message was handled successfully, false otherwise
     */
    boolean handleMessage(ThreadMessage<?> message);
    
    /**
     * Gets the winner of the game, if the game is over.
     * 
     * @return The PlayerHandler of the winner, or null if the game is not over or ended in a draw
     */
    PlayerHandler getWinner();
    
    /**
     * Resets the game state to start a new game with the same players.
     */
    void resetGame();
    
    /**
     * Pauses the game, saving the current state.
     * Called when a session enters the PAUSED state.
     */
    void pauseGame();
    
    /**
     * Resumes the game from a paused state.
     * Called when a session returns to the RUNNING state.
     */
    void resumeGame();
} 