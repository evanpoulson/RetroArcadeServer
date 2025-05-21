package server.game;

/**
 * Enum representing the possible pieces in a ConnectFour game.
 */
public enum ConnectFourPiece implements GamePiece {
    RED('R'),
    BLUE('B'),
    EMPTY(' ');

    private final char symbol;

    ConnectFourPiece(char symbol) {
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

    public static ConnectFourPiece fromSymbol(char symbol) {
        for (ConnectFourPiece piece : values()) {
            if (piece.symbol == symbol) {
                return piece;
            }
        }
        throw new IllegalArgumentException("Invalid piece symbol: " + symbol);
    }
} 