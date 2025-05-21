package server.game;

/**
 * Enum representing the possible pieces in a TicTacToe game.
 */
public enum TicTacToePiece implements GamePiece {
    X('X'),
    O('O'),
    EMPTY(' ');

    private final char symbol;

    TicTacToePiece(char symbol) {
        this.symbol = symbol;
    }

    @Override
    public char getSymbol() {
        return symbol;
    }

    @Override
    public boolean isEmpty() {
        return this == EMPTY;
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