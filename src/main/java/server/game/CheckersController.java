package server.game;

import server.player.PlayerHandler;
import server.utility.ThreadMessage;
import server.utility.MessageType;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Checkers game implementation.
 * Manages the game state, validates moves, and determines game outcomes.
 * 
 * Game Rules:
 * 1. Two players take turns moving their pieces (R and B) on an 8x8 board
 * 2. Regular pieces can only move diagonally forward one space
 * 3. Kings can move diagonally any number of spaces
 * 4. Pieces can capture opponent's pieces by jumping over them
 * 5. Multiple jumps are allowed if available
 * 6. First player to capture all opponent's pieces or block them from moving wins
 * 
 * Move Format:
 * - Moves are represented as int[4] arrays containing [fromRow, fromCol, toRow, toCol]
 * - Coordinates are 0-based (0-7 for both row and column)
 * - The top-left cell is [0,0], bottom-right is [7,7]
 */
public class CheckersController extends AbstractGameController<CheckersPiece> {
    /** The size of the game board (8x8) */
    private static final int BOARD_SIZE = 8;
    
    /** The game board, represented as a 2D array of pieces */
    private CheckersPiece[][] board;
    
    /** Map of players to their assigned pieces */
    private final Map<PlayerHandler, CheckersPiece> playerPieces;
    
    /** Flag indicating if a multiple jump is in progress */
    private boolean isMultipleJump;
    
    /** The last position from which a jump was made */
    private int[] lastJumpPosition;

    /**
     * Constructs a new Checkers game controller.
     * 
     * @param players The set of players participating in the game
     * @throws IllegalArgumentException if the number of players is not exactly 2
     */
    public CheckersController(Set<PlayerHandler> players) {
        super(players);
        if (players.size() != 2) {
            throw new IllegalArgumentException("Checkers requires exactly 2 players");
        }
        this.playerPieces = new HashMap<>();
        this.isMultipleJump = false;
        this.lastJumpPosition = null;
    }

    /**
     * Initializes the game state for a new session.
     * Sets up the board with initial piece positions and assigns Red and Blue pieces to players.
     */
    @Override
    public void initializeGame() {
        super.initializeGame();
        initializeBoard();
        
        // Assign pieces to players
        PlayerHandler[] playerArray = players.toArray(new PlayerHandler[0]);
        playerPieces.put(playerArray[0], CheckersPiece.RED);
        playerPieces.put(playerArray[1], CheckersPiece.BLUE);
    }

    /**
     * Initializes the game board with starting piece positions.
     */
    @Override
    protected void initializeBoard() {
        board = new CheckersPiece[BOARD_SIZE][BOARD_SIZE];
        
        // Initialize empty board
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = CheckersPiece.EMPTY;
            }
        }
        
        // Place Red pieces (top three rows)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if ((i + j) % 2 == 1) {
                    board[i][j] = CheckersPiece.RED;
                }
            }
        }
        
        // Place Blue pieces (bottom three rows)
        for (int i = 5; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if ((i + j) % 2 == 1) {
                    board[i][j] = CheckersPiece.BLUE;
                }
            }
        }
    }

    /**
     * Gets the piece for player 1 (Red).
     */
    @Override
    protected CheckersPiece getPlayer1Piece() {
        return CheckersPiece.RED;
    }

    /**
     * Gets the piece for player 2 (Blue).
     */
    @Override
    protected CheckersPiece getPlayer2Piece() {
        return CheckersPiece.BLUE;
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
     * Processes a move made by a player.
     * Validates the move and updates the game state accordingly.
     * 
     * @param player The player making the move
     * @param moveData The move data as an int[4] array [fromRow, fromCol, toRow, toCol]
     * @return true if the move was valid and processed successfully
     * @throws IllegalArgumentException if:
     *         - The move data is not an int array
     *         - The move coordinates are invalid
     *         - The move is not legal
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
        if (move.length != 4) {
            throw new IllegalArgumentException("Move data must contain exactly 4 elements [fromRow, fromCol, toRow, toCol]");
        }

        int fromRow = move[0];
        int fromCol = move[1];
        int toRow = move[2];
        int toCol = move[3];

        // Validate move coordinates
        if (!isValidCoordinates(fromRow, fromCol) || !isValidCoordinates(toRow, toCol)) {
            throw new IllegalArgumentException("Move coordinates must be between 0 and " + (BOARD_SIZE - 1));
        }

        // Validate piece ownership
        CheckersPiece piece = board[fromRow][fromCol];
        if (piece.isEmpty() || !isPlayerPiece(player, piece)) {
            throw new IllegalArgumentException("Invalid piece selection");
        }

        // Check if multiple jump is in progress
        if (isMultipleJump) {
            if (fromRow != lastJumpPosition[0] || fromCol != lastJumpPosition[1]) {
                throw new IllegalArgumentException("Must continue multiple jump from last position");
            }
        }

        // Validate and execute move
        if (isValidMove(fromRow, fromCol, toRow, toCol)) {
            executeMove(fromRow, fromCol, toRow, toCol);
            
            // Check for additional jumps
            if (hasAdditionalJumps(toRow, toCol)) {
                isMultipleJump = true;
                lastJumpPosition = new int[]{toRow, toCol};
            } else {
                isMultipleJump = false;
                lastJumpPosition = null;
                switchPlayer();
            }

            // Check for win condition
            if (checkWin()) {
                endGame(player);
            }
            
            return true;
        } else {
            throw new IllegalArgumentException("Invalid move");
        }
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
     * Checks if the given coordinates are valid for the board.
     * 
     * @param row The row coordinate
     * @param col The column coordinate
     * @return true if the coordinates are valid
     */
    private boolean isValidCoordinates(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Checks if the given piece belongs to the specified player.
     * 
     * @param player The player to check
     * @param piece The piece to check
     * @return true if the piece belongs to the player
     */
    private boolean isPlayerPiece(PlayerHandler player, CheckersPiece piece) {
        CheckersPiece playerPiece = playerPieces.get(player);
        return piece == playerPiece || 
               (playerPiece == CheckersPiece.RED && piece == CheckersPiece.RED_KING) ||
               (playerPiece == CheckersPiece.BLUE && piece == CheckersPiece.BLUE_KING);
    }

    /**
     * Validates if a move is legal according to checkers rules.
     * 
     * @param fromRow Starting row
     * @param fromCol Starting column
     * @param toRow Destination row
     * @param toCol Destination column
     * @return true if the move is valid
     */
    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Check if destination is empty
        if (!board[toRow][toCol].isEmpty()) {
            return false;
        }

        CheckersPiece piece = board[fromRow][fromCol];
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        // Check if move is diagonal
        if (Math.abs(rowDiff) != Math.abs(colDiff)) {
            return false;
        }

        // Regular piece movement
        if (piece == CheckersPiece.RED || piece == CheckersPiece.BLUE) {
            // Check direction (Red moves down, Blue moves up)
            if ((piece == CheckersPiece.RED && rowDiff <= 0) || 
                (piece == CheckersPiece.BLUE && rowDiff >= 0)) {
                return false;
            }

            // Regular move (one space)
            if (Math.abs(rowDiff) == 1) {
                return true;
            }

            // Jump move (two spaces)
            if (Math.abs(rowDiff) == 2) {
                int jumpRow = fromRow + rowDiff / 2;
                int jumpCol = fromCol + colDiff / 2;
                CheckersPiece jumpedPiece = board[jumpRow][jumpCol];
                return !jumpedPiece.isEmpty() && !isPlayerPiece(currentPlayer, jumpedPiece);
            }
        }
        // King movement
        else if (piece == CheckersPiece.RED_KING || piece == CheckersPiece.BLUE_KING) {
            // Check if path is clear
            int rowStep = rowDiff > 0 ? 1 : -1;
            int colStep = colDiff > 0 ? 1 : -1;
            int currentRow = fromRow + rowStep;
            int currentCol = fromCol + colStep;
            int capturedCount = 0;

            while (currentRow != toRow && currentCol != toCol) {
                CheckersPiece currentPiece = board[currentRow][currentCol];
                if (!currentPiece.isEmpty()) {
                    if (isPlayerPiece(currentPlayer, currentPiece) || capturedCount > 0) {
                        return false;
                    }
                    capturedCount++;
                }
                currentRow += rowStep;
                currentCol += colStep;
            }

            return true;
        }

        return false;
    }

    /**
     * Executes a valid move on the board.
     * 
     * @param fromRow Starting row
     * @param fromCol Starting column
     * @param toRow Destination row
     * @param toCol Destination column
     */
    private void executeMove(int fromRow, int fromCol, int toRow, int toCol) {
        CheckersPiece piece = board[fromRow][fromCol];
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        // Handle regular piece movement
        if (piece == CheckersPiece.RED || piece == CheckersPiece.BLUE) {
            // Check for jump
            if (Math.abs(rowDiff) == 2) {
                // Remove captured piece
                int jumpRow = fromRow + rowDiff / 2;
                int jumpCol = fromCol + colDiff / 2;
                board[jumpRow][jumpCol] = CheckersPiece.EMPTY;
            }

            // Move piece
            board[toRow][toCol] = piece;

            // Check for king promotion
            if ((piece == CheckersPiece.RED && toRow == BOARD_SIZE - 1) || 
                (piece == CheckersPiece.BLUE && toRow == 0)) {
                board[toRow][toCol] = piece.getKingVersion();
            }
        }
        // Handle king movement
        else if (piece == CheckersPiece.RED_KING || piece == CheckersPiece.BLUE_KING) {
            // Remove captured pieces along the path
            int rowStep = rowDiff > 0 ? 1 : -1;
            int colStep = colDiff > 0 ? 1 : -1;
            int currentRow = fromRow + rowStep;
            int currentCol = fromCol + colStep;

            while (currentRow != toRow && currentCol != toCol) {
                if (!board[currentRow][currentCol].isEmpty()) {
                    board[currentRow][currentCol] = CheckersPiece.EMPTY;
                }
                currentRow += rowStep;
                currentCol += colStep;
            }

            // Move king
            board[toRow][toCol] = piece;
        }

        // Clear original position
        board[fromRow][fromCol] = CheckersPiece.EMPTY;
    }

    /**
     * Checks if there are additional jumps available from the given position.
     * 
     * @param row Current row
     * @param col Current column
     * @return true if additional jumps are available
     */
    private boolean hasAdditionalJumps(int row, int col) {
        CheckersPiece piece = board[row][col];
        int[][] directions = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (isValidMove(row, col, newRow, newCol)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the current player has won the game.
     * A player wins if the opponent has no pieces left or cannot make a legal move.
     * 
     * @return true if the current player has won
     */
    private boolean checkWin() {
        // Find opponent
        PlayerHandler opponent = null;
        for (PlayerHandler player : players) {
            if (player != currentPlayer) {
                opponent = player;
                break;
            }
        }

        // Check if opponent has any pieces
        boolean hasPieces = false;
        boolean canMove = false;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                CheckersPiece piece = board[i][j];
                if (isPlayerPiece(opponent, piece)) {
                    hasPieces = true;
                    // Check if piece can move
                    if (canPieceMove(i, j)) {
                        canMove = true;
                        break;
                    }
                }
            }
            if (hasPieces && canMove) {
                break;
            }
        }

        return !hasPieces || !canMove;
    }

    /**
     * Checks if a piece can make any legal move.
     * 
     * @param row Current row
     * @param col Current column
     * @return true if the piece can move
     */
    private boolean canPieceMove(int row, int col) {
        CheckersPiece piece = board[row][col];
        int[][] directions;

        if (piece.isKing()) {
            directions = new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        } else {
            directions = new int[][]{{-1, -1}, {-1, 1}};
            if (piece == CheckersPiece.BLUE) {
                directions = new int[][]{{1, -1}, {1, 1}};
            }
        }

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (isValidMove(row, col, newRow, newCol)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Resets the game state to start a new game with the same players.
     * Clears the board and resets the multiple jump state.
     */
    @Override
    public void resetGame() {
        super.resetGame();
        isMultipleJump = false;
        lastJumpPosition = null;
    }
} 