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

    public TicTacToeController(Set<PlayerHandler> players) {
        super(players);
        this.board = new char[BOARD_SIZE][BOARD_SIZE];
        this.playerPieces = new HashMap<>();
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
    }

    /**
     * Gets the piece assigned to a specific player.
     * 
     * @param player The player to get the piece for
     * @return The character representing the player's piece (X or O)
     */
    public char getPlayerPiece(PlayerHandler player) {
        return playerPieces.get(player);
    }

    @Override
    public boolean processMove(PlayerHandler player, Object moveData) {
        if (!validatePlayerTurn(player)) {
            return false;
        }

        if (!(moveData instanceof int[] move) || move.length != 2) {
            return false;
        }

        int row = move[0];
        int col = move[1];

        // Validate move coordinates
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }

        // Check if cell is empty
        if (board[row][col] != EMPTY) {
            return false;
        }

        // Make the move
        board[row][col] = (player == currentPlayer) ? X : O;

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
        if (message.getType() == MessageType.MOVE_MADE) {
            return processMove(message.getPlayerSender(), message.getData());
        }
        return false;
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
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }
} 