package server.game;

/**
 * Enum representing the possible pieces in a ConnectFour game.
 */
public enum ConnectFourPiece {
    RED('R'),
    BLUE('B'),
    EMPTY(' ');

    private final char symbol;

    ConnectFourPiece(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
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