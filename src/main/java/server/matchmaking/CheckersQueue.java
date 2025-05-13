package server.matchmaking;

/**
 * Matchmaking queue for Checkers on the RetroArcade platform.
 * <p>
 * Uses a more generous expansion to match players quickly in a slower pool.
 */
public class CheckersQueue extends MatchmakingQueue {

    /** Constructs an empty Checkers matchmaking queue. */
    public CheckersQueue() {
        super();
    }

    /**
     * How much the allowed rating delta grows every {@code DELTA_EXPAND_RATE_MS}.
     * <p>
     * Checkers widens at 25 points per interval as opposed to the default 50.
     */
    @Override
    protected int getDeltaExpandAmount() {
        return 25;
    }
}

