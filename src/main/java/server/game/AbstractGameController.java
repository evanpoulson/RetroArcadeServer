package server.game;

import server.player.PlayerHandler;
import server.utility.ThreadMessage;

import java.util.Set;

/**
 * Abstract base class that implements common functionality for all game controllers.
 * Provides default implementations for some methods and enforces common game rules.
 */
public abstract class AbstractGameController implements GameController {
    protected final Set<PlayerHandler> players;
    protected PlayerHandler currentPlayer;
    protected boolean gameOver;
    protected PlayerHandler winner;
    protected boolean isPaused;

    protected AbstractGameController(Set<PlayerHandler> players) {
        this.players = players;
        this.gameOver = false;
        this.winner = null;
        this.isPaused = false;
    }

    @Override
    public void initializeGame() {
        // Default implementation: first player in the set goes first
        currentPlayer = players.iterator().next();
        gameOver = false;
        winner = null;
        isPaused = false;
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public PlayerHandler getCurrentPlayer() {
        return currentPlayer;
    }

    @Override
    public PlayerHandler getWinner() {
        return winner;
    }

    @Override
    public void resetGame() {
        initializeGame();
    }

    @Override
    public void pauseGame() {
        isPaused = true;
    }

    @Override
    public void resumeGame() {
        isPaused = false;
    }

    /**
     * Switches the current player to the other player.
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
     * 
     * @param player The player attempting to make a move
     * @return true if the player can move, false otherwise
     */
    protected boolean validatePlayerTurn(PlayerHandler player) {
        return !gameOver && !isPaused && player == currentPlayer;
    }
} 