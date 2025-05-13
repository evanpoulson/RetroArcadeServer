package server.matchmaking;

import server.player.PlayerHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class representing a matchmaking queue for a specific game in the RetroArcade platform.
 * <p>
 * This class provides core queue management functionality, including tracking when players enter the queue
 * and a built-in, adaptive rating-based matching algorithm that widens over time.
 * <p>
 * Subclasses may override {@link #getDeltaExpandAmount()} to tune how quickly the rating window grows.
 */
public abstract class MatchmakingQueue {

    /**
     * The set of players currently in the queue.
     * <p>
     * A {@link LinkedHashSet} preserves insertion order (for wait-time fairness) and prevents duplicates.
     */
    protected final Set<PlayerHandler> queue = new LinkedHashSet<>();

    /**
     * Records the timestamp (in milliseconds) when each player entered the queue.
     * <p>
     * Used to calculate wait time for adaptive rating thresholds.
     */
    protected final Map<PlayerHandler, Long> joinTimes = new ConcurrentHashMap<>();

    /** The base allowed rating delta when a player first joins. */
    protected static final int BASE_RATING_DELTA = 50;

    /**
     * Time interval (ms) after which the rating window expands.
     * <p>
     * Every {@code DELTA_EXPAND_RATE_MS}, the window grows by {@link #getDeltaExpandAmount()}.
     */
    protected static final int DELTA_EXPAND_RATE_MS = 5_000;

    /** Default increment for rating delta per {@link #DELTA_EXPAND_RATE_MS}. */
    protected static final int DELTA_EXPAND_AMOUNT = 10;

    /**
     * A simple pair of players who should be matched into a game session.
     */
    public static record MatchPair(PlayerHandler p1, PlayerHandler p2) { }

    /** Constructs an empty matchmaking queue. */
    public MatchmakingQueue() { }

    /**
     * Adds a player to the queue.
     * <p>
     * Records join time on first insertion; duplicates are ignored.
     *
     * @param player the player to enqueue
     */
    public void enqueue(PlayerHandler player) {
        if (queue.add(player)) {
            joinTimes.put(player, System.currentTimeMillis());
        }
    }

    /**
     * Removes a player from the queue and clears their join time.
     *
     * @param player the player to remove
     */
    public void remove(PlayerHandler player) {
        queue.remove(player);
        joinTimes.remove(player);
    }

    /**
     * Returns how many players are currently waiting.
     *
     * @return queue size
     */
    public int size() {
        return queue.size();
    }

    /**
     * Checks if a player is in the queue.
     *
     * @param player the player to check
     * @return {@code true} if present, {@code false} otherwise
     */
    public boolean contains(PlayerHandler player) {
        return queue.contains(player);
    }

    /**
     * How much the allowed rating delta grows every {@link #DELTA_EXPAND_RATE_MS}.
     * <p>
     * Subclasses can override to customize per-game expansion speed.
     *
     * @return rating delta increment
     */
    protected int getDeltaExpandAmount() {
        return DELTA_EXPAND_AMOUNT;
    }

    /**
     * Examines the queue and returns a list of matched player pairs based on rating and wait time.
     * <p>
     * The algorithm:
     * <ol>
     *   <li>Return immediately if fewer than two players are waiting.</li>
     *   <li>Snapshot current players in insertion order.</li>
     *   <li>For each player, compute an {@code allowedDelta} = {@link #BASE_RATING_DELTA}
     *       + ((now − joinTime) / {@link #DELTA_EXPAND_RATE_MS}) × {@link #getDeltaExpandAmount()}.</li>
     *   <li>Search forward for the first partner whose rating difference
     *       {@code |p1.getRating() − p2.getRating()|} is ≤ {@code allowedDelta}.</li>
     *   <li>Package each matched pair into a {@link MatchPair}, remove them from the live queue, and repeat.</li>
     * </ol>
     *
     * @return non-null list of {@link MatchPair}s (empty if no matches)
     */
    public List<MatchPair> tryMatch() {
        if (queue.size() < 2) {
            return Collections.emptyList();
        }

        long now = System.currentTimeMillis();
        List<PlayerHandler> snapshot = new ArrayList<>(queue);
        List<MatchPair> matches = new ArrayList<>();
        Set<PlayerHandler> matched = new HashSet<>();

        for (int i = 0; i < snapshot.size(); i++) {
            PlayerHandler p1 = snapshot.get(i);
            if (matched.contains(p1)) continue;

            long joinTime = joinTimes.getOrDefault(p1, now);
            long waited = now - joinTime;
            int allowedDelta = BASE_RATING_DELTA
                    + (int)(waited / DELTA_EXPAND_RATE_MS) * getDeltaExpandAmount();

            int rating1 = p1.getRating();

            for (int j = i + 1; j < snapshot.size(); j++) {
                PlayerHandler p2 = snapshot.get(j);
                if (matched.contains(p2)) continue;

                int rating2 = p2.getRating();
                if (Math.abs(rating1 - rating2) <= allowedDelta) {
                    matched.add(p1);
                    matched.add(p2);
                    queue.remove(p1);
                    queue.remove(p2);
                    joinTimes.remove(p1);
                    joinTimes.remove(p2);
                    matches.add(new MatchPair(p1, p2));
                    break;
                }
            }
        }

        return matches;
    }
}

