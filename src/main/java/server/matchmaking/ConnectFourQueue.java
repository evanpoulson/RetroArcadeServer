package server.matchmaking;

/**
 * Matchmaking queue for Connect Four on the RetroArcade platform.
 * <p>
 * Loosens rating constraints a bit faster to accommodate a larger player base.
 */
public class ConnectFourQueue extends MatchmakingQueue {

    /** Constructs an empty Connect Four matchmaking queue. */
    public ConnectFourQueue() {
        super();
    }

    // no override: uses BASE_DELTA + 10 per DELTA_EXPAND_RATE_MS
}

