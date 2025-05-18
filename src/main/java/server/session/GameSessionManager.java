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

public class GameSessionManager implements Runnable {
    private SessionContext context;
    private final BlockingQueue<ThreadMessage<?>> inbox;
    private GameController gameController;
    private PlayerHandler pausedBy; // Track who paused the game

    public GameSessionManager() {
        inbox = new LinkedBlockingQueue<>();
    }

    public SessionContext getContext() {
        return context;
    }

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

    private GameController createGameController(GameType gameType) {
        return switch (gameType) {
            case TicTacToe -> new TicTacToeController(context.getParticipants());
            case ConnectFour -> new ConnectFourController(context.getParticipants());
            case Checkers -> new CheckersController(context.getParticipants());
        };
    }

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
     * Only game-related messages are checked; system messages (like PAUSE, RESUME)
     * are allowed from any player.
     * 
     * @param message The message to check
     * @return true if the message is from the current player or is a system message
     */
    private boolean isMessageFromCurrentPlayer(ThreadMessage<?> message) {
        // System messages can come from any player
        if (message.getType() == MessageType.PAUSE_REQUEST || 
            message.getType() == MessageType.RESUME_REQUEST || 
            message.getType() == MessageType.DISCONNECT) {
            return true;
        }
        
        // For game messages, check if they're from the current player
        return message.getPlayerSender() == gameController.getCurrentPlayer();
    }

    private void handleDisconnect(ThreadMessage<?> message) {
        context.setState(SessionState.CANCELLED);
        // TODO: Notify other player about disconnect
    }

    private void handlePauseRequest(ThreadMessage<?> message) {
        if (context.getState() == SessionState.RUNNING && 
            message.getPlayerSender() == gameController.getCurrentPlayer()) {
            context.setState(SessionState.PAUSED);
            gameController.pauseGame();
            pausedBy = message.getPlayerSender();
            // Notify all players about pause
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<>(MessageType.GAME_PAUSED, this, null));
            }
        }
    }

    private void handleResumeRequest(ThreadMessage<?> message) {
        if (context.getState() == SessionState.PAUSED && 
            message.getPlayerSender() == pausedBy) {
            context.setState(SessionState.RUNNING);
            gameController.resumeGame();
            pausedBy = null;
            // Notify all players about resume
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<>(MessageType.GAME_RESUMED, this, null));
            }
        }
    }

    private void handleGameOver() {
        context.setState(SessionState.COMPLETED);
        PlayerHandler winner = gameController.getWinner();
        if (winner != null) {
            context.setWinner(winner.getID());
            // Notify all players about the winner
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<>(MessageType.GAME_WON, this, winner));
            }
        } else {
            // Notify all players about the draw
            for (PlayerHandler player : context.getParticipants()) {
                sendMessageToPlayer(player, new ThreadMessage<>(MessageType.GAME_DRAWN, this, null));
            }
        }
    }

    /**
     * Notifies all players about whose turn it is.
     */
    private void notifyTurnChange() {
        PlayerHandler currentPlayer = gameController.getCurrentPlayer();
        for (PlayerHandler player : context.getParticipants()) {
            if (player == currentPlayer) {
                sendMessageToPlayer(player, new ThreadMessage<>(MessageType.YOUR_TURN, this, null));
            } else {
                sendMessageToPlayer(player, new ThreadMessage<>(MessageType.OTHER_PLAYER_TURN, this, currentPlayer));
            }
        }
    }

    /**
     * Sends a "not your turn" message to a player.
     */
    private void sendNotYourTurnMessage(PlayerHandler player) {
        sendMessageToPlayer(player, new ThreadMessage<>(MessageType.NOT_YOUR_TURN, this, null));
    }

    /**
     * Sends an error message to a player.
     */
    private void sendErrorToPlayer(PlayerHandler player, String errorMessage) {
        sendMessageToPlayer(player, new ThreadMessage<>(MessageType.ERROR, this, errorMessage));
    }

    /**
     * Sends a message to a specific player.
     * This is a placeholder - actual implementation will depend on how messages
     * are sent to clients.
     */
    private void sendMessageToPlayer(PlayerHandler player, ThreadMessage<?> message) {
        // TODO: Implement actual message sending to client
        // This might involve getting the player's output queue or socket
    }
}
