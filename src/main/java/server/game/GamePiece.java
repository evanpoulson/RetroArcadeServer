package server.game;

/**
 * Interface defining the contract for all game pieces.
 * This allows different games to have their own piece implementations
 * while maintaining a common interface for the game controller.
 */
public interface GamePiece {
    /**
     * Gets the character symbol representing this piece.
     * This is used for display and communication with clients.
     * 
     * @return The character symbol for this piece
     */
    char getSymbol();

    /**
     * Checks if this piece represents an empty cell.
     * 
     * @return true if this is an empty piece, false otherwise
     */
    boolean isEmpty();
} 