package server.game;

/**
 * Enum representing the possible pieces in a Checkers game.
 */
public enum CheckersPiece {
    RED('R'),
    BLUE('B'),
    RED_KING('K'),
    BLUE_KING('Q'),
    EMPTY(' ');

    private final char symbol;

    CheckersPiece(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public boolean isKing() {
        return this == RED_KING || this == BLUE_KING;
    }

    public CheckersPiece getKingVersion() {
        if (this == RED) return RED_KING;
        if (this == BLUE) return BLUE_KING;
        return this;
    }

    public static CheckersPiece fromSymbol(char symbol) {
        for (CheckersPiece piece : values()) {
            if (piece.symbol == symbol) {
                return piece;
            }
        }
        throw new IllegalArgumentException("Invalid piece symbol: " + symbol);
    }
} 