package server.game;

import server.player.PlayerHandler;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Tic Tac Toe game implementation.
 * Manages the game state, validates moves, and determines game outcomes.
 * 
 * Game Rules:
 * 1. Two players take turns placing their pieces (X and O) on a 3x3 board
 * 2. First player to get three of their pieces in a row (horizontally, vertically, or diagonally) wins
 * 3. If all cells are filled and no player has won, the game is a draw
 * 
 * Move Format:
 * - Moves are represented as int[2] arrays containing [row, col] coordinates
 * - Coordinates are 0-based (0-2 for both row and column)
 * - The top-left cell is [0,0], bottom-right is [2,2]
 */
public class TicTacToeController extends AbstractGameController<TicTacToePiece> {
    /** The size of the game board (3x3) */
    private static final int BOARD_SIZE = 3;
    
    /** The game board, represented as a 2D array of pieces */
    private TicTacToePiece[][] board;
    
    /** Map of players to their assigned pieces (X or O) */
    private final Map<PlayerHandler, TicTacToePiece> playerPieces;

    /**
     * Constructs a new Tic Tac Toe game controller.
     * 
     * @param players The set of players participating in the game
     * @throws IllegalArgumentException if the number of players is not exactly 2
     */
    public TicTacToeController(Set<PlayerHandler> players) {
        super(players);
        if (players.size() != 2) {
            throw new IllegalArgumentException("TicTacToe requires exactly 2 players");
        }
        this.playerPieces = new HashMap<>();
    }

    /**
     * Initializes the game state for a new session.
     * Sets up an empty board and assigns X and O pieces to players.
     */
    @Override
    public void initializeGame() {
        super.initializeGame();
        // Initialize empty board
        initializeBoard();
        
        // Assign pieces to players
        PlayerHandler[] playerArray = players.toArray(new PlayerHandler[0]);
        playerPieces.put(playerArray[0], TicTacToePiece.X);
        playerPieces.put(playerArray[1], TicTacToePiece.O);
    }

    /**
     * Initializes the game board.
     * Sets up a 3x3 grid with all cells empty.
     */
    @Override
    protected void initializeBoard() {
        board = new TicTacToePiece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = TicTacToePiece.EMPTY;
            }
        }
    }

    /**
     * Gets the piece for player 1 (X).
     */
    @Override
    protected TicTacToePiece getPlayer1Piece() {
        return TicTacToePiece.X;
    }

    /**
     * Gets the piece for player 2 (O).
     */
    @Override
    protected TicTacToePiece getPlayer2Piece() {
        return TicTacToePiece.O;
    }

    /**
     * Gets the total number of cells on the board.
     * 
     * @return The total number of cells (BOARD_SIZE * BOARD_SIZE)
     */
    @Override
    public int getBoardSize() {
        return BOARD_SIZE * BOARD_SIZE;
    }

    /**
     * Gets the piece assigned to a specific player.
     * 
     * @param player The player to get the piece for
     * @return The character representing the player's piece (X or O)
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
     * Gets the current state of the game board.
     * 
     * @return The 2D array representing the current board state
     */
    @Override
    public Object getGameState() {
        return board;
    }

    /**
     * Processes a move made by a player.
     * Validates the move and updates the game state accordingly.
     * 
     * @param player The player making the move
     * @param moveData The move data as an int[2] array [row, col]
     * @return true if the move was valid and processed successfully
     * @throws IllegalArgumentException if:
     *         - The move data is not an int array
     *         - The move coordinates are invalid
     *         - The cell is already occupied
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
        if (!(moveData instanceof int[] move)) {
            throw new IllegalArgumentException("Move data must be an int array");
        }
        if (move.length != 2) {
            throw new IllegalArgumentException("Move data must contain exactly 2 elements [row, col]");
        }

        int row = move[0];
        int col = move[1];

        // Validate move coordinates
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            throw new IllegalArgumentException("Move coordinates must be between 0 and " + (BOARD_SIZE - 1));
        }

        // Check if cell is empty
        if (!board[row][col].isEmpty()) {
            throw new IllegalArgumentException("Cell is already occupied");
        }

        // Make the move
        board[row][col] = playerPieces.get(player);
        incrementMoveCount();

        // Check for win or draw
        if (checkWin(row, col)) {
            endGame(player);
        } else if (isBoardFull()) {
            endGame(null); // Draw
        } else {
            switchPlayer();
        }

        return true;
    }

    /**
     * Checks if the last move resulted in a win.
     * Checks the row, column, and diagonals (if applicable) of the last move.
     * 
     * @param lastRow The row of the last move
     * @param lastCol The column of the last move
     * @return true if the last move resulted in a win
     */
    private boolean checkWin(int lastRow, int lastCol) {
        TicTacToePiece piece = board[lastRow][lastCol];

        // Check row
        boolean rowWin = true;
        for (int col = 0; col < BOARD_SIZE; col++) {
            if (board[lastRow][col] != piece) {
                rowWin = false;
                break;
            }
        }
        if (rowWin) return true;

        // Check column
        boolean colWin = true;
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (board[row][lastCol] != piece) {
                colWin = false;
                break;
            }
        }
        if (colWin) return true;

        // Check diagonal if move is on diagonal
        if (lastRow == lastCol) {
            boolean diagWin = true;
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (board[i][i] != piece) {
                    diagWin = false;
                    break;
                }
            }
            if (diagWin) return true;
        }

        // Check anti-diagonal if move is on anti-diagonal
        if (lastRow + lastCol == BOARD_SIZE - 1) {
            boolean antiDiagWin = true;
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (board[i][BOARD_SIZE - 1 - i] != piece) {
                    antiDiagWin = false;
                    break;
                }
            }
            if (antiDiagWin) return true;
        }

        return false;
    }

    /**
     * Resets the game state to start a new game with the same players.
     * Clears the board and resets the move counter.
     */
    @Override
    public void resetGame() {
        super.resetGame();
    }
} 