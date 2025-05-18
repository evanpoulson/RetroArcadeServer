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
 * 
 * Each game implementation must provide:
 * - A way to initialize and reset the game state
 * - Move validation and processing
 * - Win/draw condition checking
 * - Game state representation
 * - Turn management
 * - Pause/resume functionality
 */
public interface GameController {
    
    /**
     * Initializes the game state for a new session.
     * This method should:
     * - Set up the initial game board/state
     * - Assign initial player turns
     * - Reset any game-specific counters or flags
     * - Prepare the game for the first move
     * 
     * Called when a new game session is created.
     */
    void initializeGame();
    
    /**
     * Processes a move made by a player.
     * This method should:
     * - Validate the move is legal
     * - Update the game state
     * - Check for win/draw conditions
     * - Update turn information if needed
     * 
     * @param player The player making the move
     * @param moveData The data representing the move (type depends on game implementation)
     * @return true if the move was valid and processed successfully, false otherwise
     * @throws IllegalArgumentException if the move data is invalid
     * @throws IllegalStateException if the game is not in a valid state for moves
     */
    boolean processMove(PlayerHandler player, Object moveData);
    
    /**
     * Checks if the game has reached a terminal state (win, loss, or draw).
     * This method should be called after each move to determine if the game should end.
     * 
     * @return true if the game is over, false if it should continue
     */
    boolean isGameOver();
    
    /**
     * Gets the current state of the game board.
     * The format of the state depends on the specific game implementation.
     * This state should be sufficient to reconstruct the game board
     * and determine the current game situation.
     * 
     * @return Object representing the current game state
     */
    Object getGameState();
    
    /**
     * Gets the player who should make the next move.
     * This method is used to enforce turn order and validate moves.
     * 
     * @return The PlayerHandler for the player whose turn it is
     */
    PlayerHandler getCurrentPlayer();
    
    /**
     * Handles a message received from a player.
     * This method processes game-specific messages and commands.
     * It should:
     * - Validate the message type and content
     * - Process the message appropriately
     * - Return success/failure status
     * 
     * @param message The message to process
     * @return true if the message was handled successfully, false otherwise
     */
    boolean handleMessage(ThreadMessage<?> message);
    
    /**
     * Gets the winner of the game, if the game is over.
     * This method should only return a non-null value if:
     * - The game is over (isGameOver() returns true)
     * - There is a clear winner (not a draw)
     * 
     * @return The PlayerHandler of the winner, or null if the game is not over or ended in a draw
     */
    PlayerHandler getWinner();
    
    /**
     * Resets the game state to start a new game with the same players.
     * This method should:
     * - Clear the game board/state
     * - Reset any game-specific counters or flags
     * - Maintain the same player assignments
     * - Prepare for a new game
     */
    void resetGame();
    
    /**
     * Pauses the game, saving the current state.
     * This method should:
     * - Preserve the current game state
     * - Prevent any moves from being processed
     * - Maintain turn information
     * 
     * Called when a session enters the PAUSED state.
     */
    void pauseGame();
    
    /**
     * Resumes the game from a paused state.
     * This method should:
     * - Restore the game to its previous state
     * - Allow moves to be processed again
     * - Maintain turn information
     * 
     * Called when a session returns to the RUNNING state.
     */
    void resumeGame();
} 