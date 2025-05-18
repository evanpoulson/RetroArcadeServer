package server.game;

import server.player.PlayerHandler;
import server.utility.ThreadMessage;
import server.utility.MessageType;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Tic Tac Toe game implementation.
 * Manages the game state, validates moves, and determines game outcomes.
 */
public class TicTacToeController extends AbstractGameController {
    private static final int BOARD_SIZE = 3;
    private char[][] board;
    private static final char EMPTY = ' ';
    private static final char X = 'X';
    private static final char O = 'O';
    private final Map<PlayerHandler, Character> playerPieces;
    private int moveCount;

    public TicTacToeController(Set<PlayerHandler> players) {
        super(players);
        if (players.size() != 2) {
            throw new IllegalArgumentException("TicTacToe requires exactly 2 players");
        }
        this.board = new char[BOARD_SIZE][BOARD_SIZE];
        this.playerPieces = new HashMap<>();
        this.moveCount = 0;
    }

    @Override
    public void initializeGame() {
        super.initializeGame();
        // Initialize empty board
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }
        
        // Assign pieces to players
        PlayerHandler[] playerArray = players.toArray(new PlayerHandler[0]);
        playerPieces.put(playerArray[0], X);
        playerPieces.put(playerArray[1], O);
        moveCount = 0;
    }

    @Override
    public boolean processMove(PlayerHandler player, Object moveData) {
        // Validate game state
        if (gameOver) {
            throw new IllegalStateException("Cannot make moves after game is over");
        }
        if (isPaused) {
            throw new IllegalStateException("Cannot make moves while game is paused");
        }

        // Validate player
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player is not part of this game");
        }
        if (!validatePlayerTurn(player)) {
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
        if (board[row][col] != EMPTY) {
            throw new IllegalArgumentException("Cell is already occupied");
        }

        // Make the move
        board[row][col] = playerPieces.get(player);
        moveCount++;

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

    @Override
    public Object getGameState() {
        return board;
    }

    @Override
    public boolean handleMessage(ThreadMessage<?> message) {
        if (message.getType() != MessageType.MOVE_MADE) {
            return false;
        }

        try {
            return processMove(message.getPlayerSender(), message.getData());
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Log the error
            System.err.println("Error processing move: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the piece assigned to a specific player.
     * 
     * @param player The player to get the piece for
     * @return The character representing the player's piece (X or O)
     * @throws IllegalArgumentException if the player is not part of this game
     */
    public char getPlayerPiece(PlayerHandler player) {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player is not part of this game");
        }
        return playerPieces.get(player);
    }

    /**
     * Checks if the last move resulted in a win.
     * 
     * @param lastRow The row of the last move
     * @param lastCol The column of the last move
     * @return true if the last move resulted in a win
     */
    private boolean checkWin(int lastRow, int lastCol) {
        char symbol = board[lastRow][lastCol];

        // Check row
        boolean rowWin = true;
        for (int col = 0; col < BOARD_SIZE; col++) {
            if (board[lastRow][col] != symbol) {
                rowWin = false;
                break;
            }
        }
        if (rowWin) return true;

        // Check column
        boolean colWin = true;
        for (int row = 0; row < BOARD_SIZE; row++) {
            if (board[row][lastCol] != symbol) {
                colWin = false;
                break;
            }
        }
        if (colWin) return true;

        // Check diagonal if move is on diagonal
        if (lastRow == lastCol) {
            boolean diagWin = true;
            for (int i = 0; i < BOARD_SIZE; i++) {
                if (board[i][i] != symbol) {
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
                if (board[i][BOARD_SIZE - 1 - i] != symbol) {
                    antiDiagWin = false;
                    break;
                }
            }
            if (antiDiagWin) return true;
        }

        return false;
    }

    /**
     * Checks if the board is full (draw condition).
     * 
     * @return true if the board is full
     */
    private boolean isBoardFull() {
        return moveCount == BOARD_SIZE * BOARD_SIZE;
    }

    @Override
    public void resetGame() {
        super.resetGame();
        moveCount = 0;
    }
} 