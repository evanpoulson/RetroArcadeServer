package server.game;

import server.player.PlayerHandler;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Connect Four game implementation.
 * Manages the game state, validates moves, and determines game outcomes.
 * 
 * Game Rules:
 * 1. Two players take turns dropping their pieces (R and B) into a 6x7 grid
 * 2. Pieces fall to the lowest available space in the chosen column
 * 3. First player to get four of their pieces in a row (horizontally, vertically, or diagonally) wins
 * 4. If all columns are filled and no player has won, the game is a draw
 * 
 * Move Format:
 * - Moves are represented as int values containing the column index (0-6)
 * - The leftmost column is 0, rightmost is 6
 * - The bottom row is 0, top row is 5
 */
public class ConnectFourController extends AbstractGameController<ConnectFourPiece> {
    /** The number of rows in the game board */
    private static final int ROWS = 6;
    
    /** The number of columns in the game board */
    private static final int COLS = 7;
    
    /** The game board, represented as a 2D array of pieces */
    private ConnectFourPiece[][] board;
    
    /** Map of players to their assigned pieces (R or B) */
    private final Map<PlayerHandler, ConnectFourPiece> playerPieces;

    /**
     * Constructs a new Connect Four game controller.
     * 
     * @param players The set of players participating in the game
     * @throws IllegalArgumentException if the number of players is not exactly 2
     */
    public ConnectFourController(Set<PlayerHandler> players) {
        super(players);
        if (players.size() != 2) {
            throw new IllegalArgumentException("Connect Four requires exactly 2 players");
        }
        this.playerPieces = new HashMap<>();
    }

    /**
     * Initializes the game state for a new session.
     * Sets up an empty board and assigns Red and Blue pieces to players.
     */
    @Override
    public void initializeGame() {
        super.initializeGame();
        // Initialize empty board
        initializeBoard();
        
        // Assign pieces to players
        PlayerHandler[] playerArray = players.toArray(new PlayerHandler[0]);
        playerPieces.put(playerArray[0], ConnectFourPiece.RED);
        playerPieces.put(playerArray[1], ConnectFourPiece.BLUE);
    }

    /**
     * Initializes the game board.
     * Sets up a 6x7 grid with all cells empty.
     */
    @Override
    protected void initializeBoard() {
        board = new ConnectFourPiece[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = ConnectFourPiece.EMPTY;
            }
        }
    }

    /**
     * Gets the piece for player 1 (Red).
     */
    @Override
    protected ConnectFourPiece getPlayer1Piece() {
        return ConnectFourPiece.RED;
    }

    /**
     * Gets the piece for player 2 (Blue).
     */
    @Override
    protected ConnectFourPiece getPlayer2Piece() {
        return ConnectFourPiece.BLUE;
    }

    /**
     * Gets the total number of cells on the board.
     * 
     * @return The total number of cells (ROWS * COLS)
     */
    @Override
    public int getBoardSize() {
        return ROWS * COLS;
    }

    /**
     * Processes a move made by a player.
     * Validates the move and updates the game state accordingly.
     * 
     * @param player The player making the move
     * @param moveData The move data as an int representing the column index
     * @return true if the move was valid and processed successfully
     * @throws IllegalArgumentException if:
     *         - The move data is not an integer
     *         - The column index is invalid
     *         - The column is full
     *         - The player is not part of this game
     * @throws IllegalStateException if:
     *         - The game is over
     *         - The game is paused
     */
    @Override
    public boolean processMove(PlayerHandler player, Object moveData) {
        // Validate game state and player
        validateGameState();
        if (!validatePlayer(player)) {
            return false;
        }

        // Validate move data
        if (!(moveData instanceof Integer column)) {
            throw new IllegalArgumentException("Move data must be an integer");
        }
        if (column < 0 || column >= COLS) {
            throw new IllegalArgumentException("Column index must be between 0 and " + (COLS - 1));
        }

        // Check if column is full
        if (board[0][column] != ConnectFourPiece.EMPTY) {
            throw new IllegalArgumentException("Column is full");
        }

        // Find the lowest empty row in the chosen column
        int row = findLowestEmptyRow(column);
        
        // Make the move
        board[row][column] = playerPieces.get(player);
        incrementMoveCount();

        // Check for win or draw
        if (checkWin(row, column)) {
            endGame(player);
        } else if (isBoardFull()) {
            endGame(null); // Draw
        } else {
            switchPlayer();
        }

        return true;
    }

    /**
     * Gets the current state of the game board.
     * 
     * @return The 2D array representing the current board state
     */
    @Override
    public Object getGameState() {
        return board;
    }

    /**
     * Gets the piece assigned to a specific player.
     * 
     * @param player The player to get the piece for
     * @return The character representing the player's piece (R or B)
     * @throws IllegalArgumentException if the player is not part of this game
     */
    @Override
    public char getPlayerPiece(PlayerHandler player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player is not part of this game");
        }
        return playerPieces.get(player).getSymbol();
    }

    /**
     * Finds the lowest empty row in the given column.
     * 
     * @param column The column to check
     * @return The row index of the lowest empty space
     */
    private int findLowestEmptyRow(int column) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][column] == ConnectFourPiece.EMPTY) {
                return row;
            }
        }
        throw new IllegalStateException("Column is full"); // This should never happen due to earlier validation
    }

    /**
     * Checks if the last move resulted in a win.
     * Checks horizontal, vertical, and both diagonal directions.
     * 
     * @param lastRow The row of the last move
     * @param lastCol The column of the last move
     * @return true if the last move resulted in a win
     */
    private boolean checkWin(int lastRow, int lastCol) {
        ConnectFourPiece piece = board[lastRow][lastCol];

        // Check horizontal
        int count = 0;
        for (int col = Math.max(0, lastCol - 3); col <= Math.min(COLS - 1, lastCol + 3); col++) {
            if (board[lastRow][col] == piece) {
                count++;
                if (count == 4) return true;
            } else {
                count = 0;
            }
        }

        // Check vertical
        count = 0;
        for (int row = Math.max(0, lastRow - 3); row <= Math.min(ROWS - 1, lastRow + 3); row++) {
            if (board[row][lastCol] == piece) {
                count++;
                if (count == 4) return true;
            } else {
                count = 0;
            }
        }

        // Check diagonal (top-left to bottom-right)
        count = 0;
        for (int i = -3; i <= 3; i++) {
            int row = lastRow + i;
            int col = lastCol + i;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (board[row][col] == piece) {
                    count++;
                    if (count == 4) return true;
                } else {
                    count = 0;
                }
            }
        }

        // Check diagonal (top-right to bottom-left)
        count = 0;
        for (int i = -3; i <= 3; i++) {
            int row = lastRow + i;
            int col = lastCol - i;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                if (board[row][col] == piece) {
                    count++;
                    if (count == 4) return true;
                } else {
                    count = 0;
                }
            }
        }

        return false;
    }

    /**
     * Resets the game state to start a new game with the same players.
     * Clears the board.
     */
    @Override
    public void resetGame() {
        super.resetGame();
    }
} 