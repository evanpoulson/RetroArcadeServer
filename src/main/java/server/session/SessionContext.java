package server.session;

import server.player.PlayerHandler;
import server.utility.GameType;
import server.utility.ThreadMessage;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Holds all of the runtime state for one live game session on the RetroArcade platform.
 * <p>
 * Tracks the session's unique identifier, game type, managing controller, participants,
 * incoming message queue, timing, lifecycle state, and eventual winner.
 */
public class SessionContext {

    /** Uniquely identifies this session. */
    private final int sessionID;
    private static final java.util.concurrent.atomic.AtomicInteger SESSION_ID_GENERATOR = new java.util.concurrent.atomic.AtomicInteger(1);

    /** The game being played (TicTacToe, ConnectFour, Checkers). */
    private final GameType gameType;

    /** The manager driving the game logic for this session. */
    private final GameSessionManager manager;

    /** The player handlers participating in this session. */
    private final Set<PlayerHandler> participants;

    /** Incoming message queue for this session's game loop. */
    private final BlockingQueue<ThreadMessage<?>> inbox = new LinkedBlockingQueue<>();

    /** Timestamp when this session was created—used for duration and timeout checks. */
    private final Instant startTime = Instant.now();

    /** Current lifecycle state of the session. */
    private SessionState state;

    /** The identifier of the winning player, or {@code null} if the game is unfinished or a draw. */
    private String winner;

    /**
     * Constructs a new session context.
     *
     * @param gameType     which game type this session is for
     * @param manager      the {@link GameSessionManager} responsible for this session
     * @param participants the set of {@link PlayerHandler}s in this session
     */
    public SessionContext(GameType gameType,
                          GameSessionManager manager,
                          Set<PlayerHandler> participants) {
        this.sessionID    = SESSION_ID_GENERATOR.getAndIncrement();
        this.gameType     = gameType;
        this.manager      = manager;
        this.participants = participants;
        this.state        = SessionState.INITIALIZING;
        this.winner       = null;
    }

    /** @return the unique session ID */
    public int getSessionID() {
        return sessionID;
    }

    /** @return the type of game being played */
    public GameType getGameType() {
        return gameType;
    }

    /** @return the manager driving this session's game logic */
    public GameSessionManager getManager() {
        return manager;
    }

    /** @return the set of players participating in this session */
    public Set<PlayerHandler> getParticipants() {
        return participants;
    }

    /** @return the queue for incoming {@link ThreadMessage}s */
    public BlockingQueue<ThreadMessage<?>> getInbox() {
        return inbox;
    }

    /** @return the instant when this session was started */
    public Instant getStartTime() {
        return startTime;
    }

    /** @return the current {@link SessionState} of this session */
    public SessionState getState() {
        return state;
    }

    /** @return the winner's player ID, or {@code null} if none yet */
    public String getWinner() {
        return winner;
    }

    /**
     * Updates the lifecycle state of this session.
     *
     * @param state the new {@link SessionState}
     */
    public void setState(SessionState state) {
        this.state = state;
    }

    /**
     * Records the winner of the session.
     *
     * @param winner the winning player's ID, or {@code null} for a draw
     */
    public void setWinner(String winner) {
        this.winner = winner;
    }

    /**
     * Calculates how long this session has been active.
     *
     * @return a {@link Duration} from {@link #startTime} to now
     */
    public Duration getDuration() {
        return Duration.between(startTime, Instant.now());
    }
}



