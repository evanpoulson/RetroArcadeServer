package server.player;

import server.profile.Profile;
import server.utility.ThreadMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import static server.utility.ServerLogger.logError;

public class PlayerHandler {
    private final Socket clientSocket;
    // todo: erm this man
    private final BlockingQueue<ThreadMessage> queue;
    private final Profile profile;
    private boolean running;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private Thread gameSessionManagerThread = null;
    private final Object gameSessionLock = new Object();
    private Thread mainThread = null;

    /**
     * Get the Thread that the PlayerHandler is on
     * @return the mainThread for the PlayerHandler
     */
    public synchronized Thread getThread() {
        return mainThread;
    }

    /**
     *
     * @return
     */
    public int getRating() {
        return 0;
    }

    /**
     *
     * @return
     */
    public int getID() {
        return 0;
    }

    /**
     * Set the Thread of the GameSessionManager
     * @param thread the new GameSessionManagerThread
     */
    public synchronized void setGameSessionManagerThread(Thread thread) {
        //TODO: this should be the session id, because that's how we lookup using the session registry.
        synchronized (gameSessionLock) {
            // Update the gameSessionManagerThread then notify gameSessionLock
            gameSessionManagerThread = thread;
            if (thread != null) {
                // If the thread is not null, notifyAll
                gameSessionLock.notifyAll();
            }
        }
    }

    /**
     * Get the Profile for the player
     * @return the Profile object for the player
     */
    public synchronized Profile getProfile() {
        return profile;
    }

    private synchronized void sendFriendRequest() {}

    private synchronized void acceptFriendUpdate() {}

    private synchronized void sendGameRequest() {}

    private synchronized void acceptGameRequest() {}

    private Object getBio() {return null;}

    private Object getNickname() {return null;}

    private Object getUsername() {return null;}

    private Object getProfilePath() {return null;}

    private Object getFriends() {return null;}

    private Object getFriendRequest() {return null;}

    private Object getGameHistory() {return null;}

    private Object getWinLossRatio() {return null;}

    private Object getRank() {return null;}

    private Object getWins() {return null;}

    private Object viewProfile() {return null;}

    /**
     * Disconnects the player from the server
     */
    private void disconnectPlayer() {}

    // todo: evan made this
    private void sendToClient(ThreadMessage message) {
        try {
            // String jsonString = JsonConverter.toJson(message.getContent());
            // printWriter.println(jsonString);
            printWriter.flush();
        } catch (IllegalArgumentException e) {
            logError("PlayerHandler: " + this.getProfile().getUsername() + " could not send message to client.");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Constructs a new PlayerHandler to manage communication with a connected client.
     *
     * @param clientSocket the socket used to communicate with the client
     * @param queue the message queue for handling communication between threads
     * @param profile the player's profile associated with this connection
     */
    public PlayerHandler(Socket clientSocket, BlockingQueue<ThreadMessage> queue, Profile profile) {
        this.clientSocket = clientSocket;
        //Create a dedicated queue for messages related to this player's thread.
        this.queue = queue;
        this.profile = profile;
        this.running = true;
        // Initialize the BufferedReader and BufferedWriter
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            logError("PlayerHandler: Failure to initialize PlayerHandler BufferedReader/BufferedWriter:", e.toString());
        }
    }

    /**
     * The main thread execution method for the PlayerHandler.
     * Listens to the blocking queue for messages and sends them to the client.
     * Also starts a listener thread to handle incoming messages from the client,
     * and sets the player's online status to true when the session begins.
     */
    public void run() {
        // Assign the mainThread to the thread created by ConnectionManager
        mainThread = Thread.currentThread();
        // Start the PlayerHandlerListener thread
        PlayerHandlerListener playerHandlerListener = new PlayerHandlerListener();
        Thread playerHandlerListenerThread = Thread.ofVirtual().start(playerHandlerListener);

        //Set the players online status to true
//        try {
//            this.getProfile().setOnlineStatus(true);
//        } catch (SQLException e) {
//            logError("PlayerHandler: " + this.getProfile().getUsername() + " could not set online status to true.");
//        }
        while (running) {
            try {
                // Take a message from the blocking queue
                ThreadMessage threadMessage = queue.take();
                // Convert to json formatting then send it to the client
                // printWriter.println(toJson(threadMessage.getContent()));
            } catch (InterruptedException e) {
                logError("PlayerHandler: Failure to take message blocking queue for PlayerHandler:", e.toString());
            }
        }
    }

    /**
     * A listener class that runs in its own thread to handle incoming messages from the client.
     * It processes JSON-formatted messages and routes them to the appropriate system components,
     * such as the matchmaking queue or the game session manager.
     */
    private class PlayerHandlerListener implements Runnable {
        /**
         * The function that the thread runs, listens to the input from the client
         */
        public void run() {
            while (running) {
                try {
                    // Read the json string from the server
                    String message = bufferedReader.readLine();

                    //If message == null, then that means the player has disconnected and this thread should be terminated.
                    if (message == null) {
                        // Disconnection
                        disconnectPlayer();
                        break;
                    }

                    // Convert the json formatting and send it to the GameSessionManager
                    try {
                        Map<String, Object> jsonMap = null; // fromJson(message);
                        // TODO: TEMPORARY, CHECK IF TYPE IS ENQUEUE
                        if (jsonMap.containsKey("type") && jsonMap.get("type").equals("enqueue")) {
                            if (jsonMap.containsKey("game-type") && jsonMap.get("game-type").equals(0)) {
                                // ServerController.enqueuePlayer(PlayerHandler.this, 0);
                            } else if (jsonMap.containsKey("game-type") && jsonMap.get("game-type").equals(1)) {
                                // ServerController.enqueuePlayer(PlayerHandler.this, 1);
                            } else if (jsonMap.containsKey("game-type") && jsonMap.get("game-type").equals(2)) {
                                // ServerController.enqueuePlayer(PlayerHandler.this, 2);
                            } else {
                                logError("PlayerHandler: Unknown game-type: " + jsonMap.get("game-type"));
                            }
                        }
                        // TODO: this threadMessage has to be like "sorted" to where it needs to go
                        // ThreadMessage threadMessage = new ThreadMessage(MessageType.NONE, mainThread, jsonMap);
                        // TODO: this code should only run if we are sending information to the gameSessionManager
                        routeMessage(null); // threadMessage);
                    } catch (IllegalArgumentException e) {
                        // TODO: Should this be handled better? wait maybe send back a message?
                        logError("PlayerHandler: Failure to parse message:", e.toString());
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } catch (IOException e) {
                    disconnectPlayer();
                    break;
                }
//                catch (InterruptedException e) {
//                    // just gotta say this should be passed in through some way man, maybe a function? idk doesn't matter
//                    log("PlayerHandler: Never received the thread for the GameSessionManager:", e.toString());
//                }
            }
        }
    }

    private void routeMessage(ThreadMessage threadMessage) throws SQLException, IOException {}
}
