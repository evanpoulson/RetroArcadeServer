package server.session;

import server.matchmaking.MatchmakingManager;
import server.player.PlayerHandler;
import server.utility.GameType;

import java.util.Set;

/**
 * Responsible for spawning new game sessions and routing players
 * into matchmaking on the RetroArcade server.
 * <p>
 * Implements a virtual‐thread “game loop” that periodically pulls
 * completed match pairs and creates sessions for them.
 */
public class GameCreator implements Runnable {

    private final MatchmakingManager matchmakingManager;

    /**
     * Constructs a GameCreator, starts its matchmaking loop,
     * and holds a reference to the shared MatchmakingManager.
     */
    public GameCreator() {
        this.matchmakingManager = new MatchmakingManager();
        Thread.ofVirtual()
                .name("RetroArcadeServer-GameCreator")
                .start(this);
    }

    /**
     * Enqueues a player into the matchmaking queue for the specified game.
     * <p>
     * Called by client‐handling code when a player requests to join matchmaking.
     *
     * @param gameType the type of game to enqueue for
     * @param player   the player handler to add to the queue
     */
    public void enqueue(GameType gameType, PlayerHandler player) {
        matchmakingManager.enqueue(gameType, player);
    }

    /**
     * Removes a player from the matchmaking queue for the specified game.
     * <p>
     * Called when a player cancels matchmaking or disconnects.
     *
     * @param gameType the type of game to dequeue from
     * @param player   the player handler to remove from the queue
     */
    public void dequeue(GameType gameType, PlayerHandler player) {
        matchmakingManager.remove(gameType, player);
    }

    /**
     * Creates and starts a new game session for two matched players.
     *
     * @param p1       first player in the match
     * @param p2       second player in the match
     * @param gameType the game they should play
     * @return {@code true} if the session was successfully launched
     */
    public boolean createGameSession(PlayerHandler p1,
                                     PlayerHandler p2,
                                     GameType gameType) {
        try {
            GameSessionManager gameSession = new GameSessionManager();

            String sessionID = p1.getID() + ":" + p2.getID();
            Set<PlayerHandler> participants = Set.of(p1, p2);

            SessionContext context =
                    new SessionContext(sessionID, gameType, gameSession, participants);
            gameSession.setContext(context);

            Thread.ofVirtual()
                    .name("gameSessionManager-" + sessionID)
                    .start(gameSession);

            SessionRegistry sessionReg = SessionRegistry.getInstance();
            sessionReg.register(context);

            return true;
        } catch (Exception e) {
            // log exception
            return false;
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 1. Pull matches
                var matches = matchmakingManager.collectAllMatches();
                // 2. Create sessions
                for (var m : matches) {
                    createGameSession(m.p1(), m.p2(), m.gameType());
                }
                // 3. Wait one second before next tick
                Thread.sleep(1_000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // graceful shutdown
        } catch (Throwable t) {
            t.printStackTrace(); // log and keep going
        }
    }
}
