package server.session;

import server.game.GameController;
import server.game.TicTacToeController;
import server.player.PlayerHandler;
import server.utility.GameType;
import server.utility.MessageType;
import server.utility.ThreadMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.HashMap;

/**
 * Manages a game session between players.
 * Handles message routing, turn management, and game state updates.
 * 
 * Responsibilities:
 * 1. Message Processing:
 *    - Routes messages between players and game controller
 *    - Validates message sources and types
 *    - Handles system messages (pause, resume, disconnect)
 * 
 * 2. Game State Management:
 *    - Initializes and maintains game state
 *    - Broadcasts state updates to players
 *    - Tracks game progress and outcomes
 * 
 * 3. Player Management:
 *    - Tracks current player's turn
 *    - Handles player disconnections
 *    - Manages player piece assignments
 * 
 * 4. Session Lifecycle:
 *    - Manages session state transitions
 *    - Handles session cleanup
 *    - Coordinates with SessionRegistry
 */
public class GameSessionManager implements Runnable {
    /** The session context containing game and player information */
    private SessionContext context;
    
    /** Queue for incoming messages from players */
    private final BlockingQueue<ThreadMessage<?>> inbox;
    
    /** The game controller managing the specific game implementation */
    private GameController gameController;
    
    /** The player who paused the game, if any */
    private PlayerHandler pausedBy;

    /**
     * Constructs a new game session manager.
     * Initializes the message inbox queue.
     */
    public GameSessionManager() {
        inbox = new LinkedBlockingQueue<>();
    }

    /**
     * Gets the current session context.
     * 
     * @return The current session context
     */
    public SessionContext getContext() {
        return context;
    }

    /**
     * Sets the session context and initializes the game.
     * Creates the appropriate game controller and sends initial game state to players.
     * 
     * @param context The session context to set
     */
    public void setContext(SessionContext context) {
        this.context = context;
        // Initialize the appropriate game controller based on game type
        this.gameController = createGameController(context.getGameType());
        this.gameController.initializeGame();
        
        // Send initial game state and piece assignments to players
        for (PlayerHandler player : context.getParticipants()) {
            Map<String, Object> gameInfo = new HashMap<>();
            gameInfo.put("gameState", gameController.getGameState());
            
            if (gameController instanceof TicTacToeController) {
                gameInfo.put("playerPiece", ((TicTacToeController)gameController).getPlayerPiece(player));
            }
            
            sendMessageToPlayer(player, new ThreadMessage<Map<String, Object>>(MessageType.GAME_STATE_UPDATE, this, gameInfo));
        }
        
        // Notify players about initial turn
        notifyTurnChange();
    }

    /**
     * Creates a game controller based on the game type.
     * 
     * @param gameType The type of game to create
     * @return A new game controller instance
     * @throws IllegalArgumentException if the game type is not supported
     */
    private GameController createGameController(GameType gameType) {
        return switch (gameType) {
            case TicTacToe -> new TicTacToeController(context.getParticipants());
            case ConnectFour -> new ConnectFourController(context.getParticipants());
            case Checkers -> new CheckersController(context.getParticipants());
        };
    }

    /**
     * Main game loop that processes messages and manages the game session.
     * Handles:
     * - Message routing
     * - Turn management
     * - Game state updates
     * - Session state transitions
     * - Error handling
     * - Session cleanup
     */
    @Override
    public void run() {
        try {
            context.setState(SessionState.RUNNING);
            
            while (!Thread.currentThread().isInterrupted()) {
                // Process messages from the inbox
                ThreadMessage<?> message = inbox.take();
                
                // Handle session state changes
                if (message.getType() == MessageType.DISCONNECT) {
                    handleDisconnect(message);
                    continue;
                } else if (message.getType() == MessageType.PAUSE_REQUEST) {
                    handlePauseRequest(message);
                    continue;
                } else if (message.getType() == MessageType.RESUME_REQUEST) {
                    handleResumeRequest(message);
                    continue;
                }
                
                // Only process game messages if the session is running
                if (context.getState() == SessionState.RUNNING) {
                    // Check if the message is from the current player
                    if (isMessageFromCurrentPlayer(message)) {
                        // Let the game controller handle the message
                        if (!gameController.handleMessage(message)) {
                            // Handle invalid message
                            sendErrorToPlayer(message.getPlayerSender(), "Invalid move");
                        } else {
                            // Valid move was made, broadcast updated game state
                            broadcastGameState();
                            
                            // Check if game is over
                            if (gameController.isGameOver()) {
                                handleGameOver();
                                break;
                            } else {
                                // Notify players about turn change
                                notifyTurnChange();
                            }
                        }
                    } else {
                        // Message from wrong player - notify them
                        sendNotYourTurnMessage(message.getPlayerSender());
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            context.setState(SessionState.CANCELLED);
        } catch (Exception e) {
            // Log error
            context.setState(SessionState.CANCELLED);
        } finally {
            // Cleanup
            SessionRegistry.getInstance().deregister(context.getSessionID());
        }
    }

    /**
     * Broadcasts the current game state to all players.
     * Includes both the game state and player-specific information.
     */
    private void broadcastGameState() {
        Map<String, Object> gameInfo = new HashMap<>();
        gameInfo.put("gameState", gameController.getGameState());
        
        for (PlayerHandler player : context.getParticipants()) {
            if (gameController instanceof TicTacToeController) {
                gameInfo.put("playerPiece", ((TicTacToeController)gameController).getPlayerPiece(player));
            }
            sendMessageToPlayer(player, new ThreadMessage<Map<String, Object>>(MessageType.GAME_STATE_UPDATE, this, gameInfo));
        }
    }

    /**
     * Checks if the message is from the player whose turn it is.
     * System messages and game state updates can come from any player.
     * Only game moves are restricted to the current player.
     * Any player can pause the game, but only the player who paused it can resume.
     * 
     * @param message The message to check
     * @return true if the message is from the current player or is a system/game state message
     */
    private boolean isMessageFromCurrentPlayer(ThreadMessage<?> message) {
        return switch (message.getType()) {
            case PAUSE_REQUEST, DISCONNECT, ERROR,
                 GAME_PAUSED, GAME_RESUMED, GAME_WON, GAME_DRAWN,
                 GAME_STATE_UPDATE, YOUR_TURN, OTHER_PLAYER_TURN,
                 NOT_YOUR_TURN -> true;
            case RESUME_REQUEST -> message.getPlayerSender() == pausedBy;
            case MOVE_MADE -> message.getPlayerSender() == gameController.getCurrentPlayer();
            default -> false;
        };
    }

    /**
     * Handles a player disconnection.
     * Cancels the session and notifies other players.
     * 
     * @param message The disconnect message
     */
    private void handleDisconnect(ThreadMessage<?> message) {
        context.setState(SessionState.CANCELLED);
        // TODO: Notify other player about disconnect
    }

    /**
     * Handles a pause request from a player.
     * Only the current player can pause the game.
     * 
     * @param message The pause request message
     */
    private void handlePauseRequest(ThreadMessage<?> message) {
        if (context.getState() == SessionState.RUNNING && 
            message.getPlayerSender() == gameController.getCurrentPlayer()) {
            context.setState(SessionState.PAUSED);
            gameController.pauseGame();
            pausedBy = message.getPlayerSender();
            // Notify all players about pause
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<Void>(MessageType.GAME_PAUSED, this, null));
            }
        }
    }

    /**
     * Handles a resume request from a player.
     * Only the player who paused the game can resume it.
     * 
     * @param message The resume request message
     */
    private void handleResumeRequest(ThreadMessage<?> message) {
        if (context.getState() == SessionState.PAUSED && 
            message.getPlayerSender() == pausedBy) {
            context.setState(SessionState.RUNNING);
            gameController.resumeGame();
            pausedBy = null;
            // Notify all players about resume
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<Void>(MessageType.GAME_RESUMED, this, null));
            }
        }
    }

    /**
     * Handles the end of the game.
     * Notifies players about the winner or draw.
     */
    private void handleGameOver() {
        context.setState(SessionState.COMPLETED);
        PlayerHandler winner = gameController.getWinner();
        if (winner != null) {
            context.setWinner(winner.getID());
            // Notify all players about the winner
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<PlayerHandler>(MessageType.GAME_WON, this, winner));
            }
        } else {
            // Notify all players about the draw
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<Void>(MessageType.GAME_DRAWN, this, null));
            }
        }
    }

    /**
     * Notifies all players about whose turn it is.
     * Sends YOUR_TURN to the current player and OTHER_PLAYER_TURN to others.
     */
    private void notifyTurnChange() {
        PlayerHandler currentPlayer = gameController.getCurrentPlayer();
        for (PlayerHandler player : context.getParticipants()) {
            if (player == currentPlayer) {
                sendMessageToPlayer(player, new ThreadMessage<Void>(MessageType.YOUR_TURN, this, null));
            } else {
                sendMessageToPlayer(player, new ThreadMessage<Void>(MessageType.OTHER_PLAYER_TURN, this, null));
            }
        }
    }

    /**
     * Sends a "not your turn" message to a player.
     * 
     * @param player The player to notify
     */
    private void sendNotYourTurnMessage(PlayerHandler player) {
        sendMessageToPlayer(player, new ThreadMessage<Void>(MessageType.NOT_YOUR_TURN, this, null));
    }

    /**
     * Sends an error message to a player.
     * 
     * @param player The player to notify
     * @param errorMessage The error message to send
     */
    private void sendErrorToPlayer(PlayerHandler player, String errorMessage) {
        sendMessageToPlayer(player, new ThreadMessage<String>(MessageType.ERROR, this, errorMessage));
    }

    /**
     * Sends a message to a specific player.
     * This is a placeholder - actual implementation will depend on how messages
     * are sent to clients.
     * 
     * @param player The player to send the message to
     * @param message The message to send
     */
    private void sendMessageToPlayer(PlayerHandler player, ThreadMessage<?> message) {
        // TODO: Implement actual message sending to client
        // This might involve getting the player's output queue or socket
    }
}
