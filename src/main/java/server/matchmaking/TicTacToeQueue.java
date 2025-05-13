package server.matchmaking;

/**
 * Matchmaking queue for Tic-Tac-Toe on the RetroArcade platform.
 * <p>
 * Uses the default rating-delta expansion rate defined in {@link MatchmakingQueue}.
 */
public class TicTacToeQueue extends MatchmakingQueue {

    /** Constructs an empty Tic-Tac-Toe matchmaking queue. */
    public TicTacToeQueue() {
        super();
    }

    /**
     * How much the allowed rating delta grows every {@code DELTA_EXPAND_RATE_MS}.
     * <p>
     * TicTacToe widens at 75 points per interval instead of the default 50.
     */
    @Override
    protected int getDeltaExpandAmount() {
        return 75;
    }
}

