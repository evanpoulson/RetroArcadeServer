package server.game;

/**
 * Enum representing the possible pieces in a TicTacToe game.
 */
public enum TicTacToePiece {
    X('X'),
    O('O'),
    EMPTY(' ');

    private final char symbol;

    TicTacToePiece(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public static TicTacToePiece fromSymbol(char symbol) {
        for (TicTacToePiece piece : values()) {
            if (piece.symbol == symbol) {
                return piece;
            }
        }
        throw new IllegalArgumentException("Invalid piece symbol: " + symbol);
    }
} 