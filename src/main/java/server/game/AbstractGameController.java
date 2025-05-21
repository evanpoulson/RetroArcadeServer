package server.game;

import server.player.PlayerHandler;
import server.utility.ThreadMessage;
import server.utility.MessageType;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * Abstract base class that implements common functionality for all game controllers.
 * Provides default implementations for some methods and enforces common game rules.
 * 
 * This class handles:
 * 1. Basic game state management (running, paused, over)
 * 2. Player turn tracking
 * 3. Winner tracking
 * 4. Common validation rules
 * 5. Board management
 * 6. Move counting
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

    /** The game board, represented as a 2D array of pieces */
    protected GamePiece[][] board;
    
    /** Map of players to their assigned pieces */
    protected final Map<PlayerHandler, GamePiece> playerPieces;
    
    /** Counter for the number of moves made in the game */
    protected int moveCount;

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
        this.playerPieces = new HashMap<>();
        this.moveCount = 0;
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
        resetMoveCount();
        initializeBoard();
        assignPieces();
    }

    /**
     * Initializes the game board.
     * Must be implemented by subclasses to set up their specific board.
     */
    protected abstract void initializeBoard();

    /**
     * Gets the piece for player 1.
     * Must be implemented by subclasses to define their piece types.
     */
    protected abstract GamePiece getPlayer1Piece();

    /**
     * Gets the piece for player 2.
     * Must be implemented by subclasses to define their piece types.
     */
    protected abstract GamePiece getPlayer2Piece();

    /**
     * Assigns pieces to players.
     * First player gets player 1's piece, second player gets player 2's piece.
     */
    protected void assignPieces() {
        PlayerHandler[] playerArray = players.toArray(new PlayerHandler[0]);
        playerPieces.put(playerArray[0], getPlayer1Piece());
        playerPieces.put(playerArray[1], getPlayer2Piece());
    }

    /**
     * Resets the move counter to zero.
     */
    protected void resetMoveCount() {
        moveCount = 0;
    }

    /**
     * Increments the move counter.
     */
    protected void incrementMoveCount() {
        moveCount++;
    }

    /**
     * Validates the current game state.
     * 
     * @throws IllegalStateException if the game is over or paused
     */
    protected boolean validateGameState() {
        if (gameOver) {
            throw new IllegalStateException("Cannot make moves after game is over");
        }
        if (isPaused) {
            throw new IllegalStateException("Cannot make moves while game is paused");
        }
        return true;
    }

    /**
     * Validates that the given player is allowed to make a move.
     * 
     * @param player The player attempting to make a move
     * @throws IllegalArgumentException if the player is not part of this game
     * @return true if the player can move, false otherwise
     */
    protected boolean validatePlayer(PlayerHandler player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player is not part of this game");
        }
        return validatePlayerTurn(player);
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
     * Gets the current state of the game board.
     * 
     * @return The 2D char array representing the current board state
     */
    @Override
    public Object getGameState() {
        return board;
    }

    /**
     * Gets the piece assigned to a specific player.
     * 
     * @param player The player to get the piece for
     * @return The character representing the player's piece
     * @throws IllegalArgumentException if the player is not part of this game
     */
    public char getPlayerPiece(PlayerHandler player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player is not part of this game");
        }
        return playerPieces.get(player).getSymbol();
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

    /**
     * Checks if the board is full (draw condition).
     * Uses the move counter for efficiency.
     * 
     * @return true if the board is full
     */
    public boolean isBoardFull() {
        return moveCount == getBoardSize();
    }

    /**
     * Gets the total number of cells on the board.
     * Must be implemented by subclasses to return their board size.
     * 
     * @return The total number of cells (rows * columns)
     */
    public abstract int getBoardSize();

    /**
     * Handles a message received from a player.
     * Currently only processes MOVE_MADE messages.
     * 
     * @param message The message to process
     * @return true if the message was handled successfully, false otherwise
     */
    @Override
    public boolean handleMessage(ThreadMessage<?> message) {
        if (message.getType() != MessageType.MOVE_MADE) {
            return false;
        }

        try {
            return processMove(message.getPlayerSender(), message.getData());
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Log the error
            System.err.println("Error processing move: " + e.getMessage());
            return false;
        }
    }
} 