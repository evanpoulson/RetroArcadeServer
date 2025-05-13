package server.matchmaking;

import server.player.PlayerHandler;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class representing a matchmaking queue for a specific game in the RetroArcade platform.
 * <p>
 * This class provides core queue management functionality, including tracking when players enter the queue
 * and providing a framework for subclasses to define custom matchmaking logic based on player attributes,
 * such as rating and time spent waiting.
 * <p>
 * Subclasses are responsible for implementing the {@link #tryMatch()} method, which contains the actual
 * matching strategy for a specific game.
 */
public abstract class MatchmakingQueue {

    /**
     * The set of players currently in the queue.
     * <p>
     * A {@link LinkedHashSet} is used to preserve insertion order and prevent duplicate entries.
     */
    protected final Set<PlayerHandler> queue = new LinkedHashSet<>();

    /**
     * Maps each player to the time (in milliseconds) when they entered the queue.
     * <p>
     * Used to calculate how long a player has been waiting and to gradually relax matching constraints.
     */
    protected final Map<PlayerHandler, Long> joinTimes = new ConcurrentHashMap<>();

    /**
     * The base allowed rating delta for matching players.
     * <p>
     * This value represents the initial strictness in rating difference when players first join the queue.
     */
    protected static final int BASE_RATING_DELTA = 50;

    /**
     * The rate at which the allowed rating delta expands over time, in milliseconds.
     * <p>
     * For example, if set to 5000 ms, the allowed rating difference increases every 5 seconds.
     */
    protected static final int DELTA_EXPAND_RATE_MS = 5000;

    /**
     * The amount by which the rating delta increases every {@code DELTA_EXPAND_RATE_MS} milliseconds.
     */
    protected static final int DELTA_EXPAND_AMOUNT = 10;

    /**
     * Constructs a new empty matchmaking queue.
     */
    public MatchmakingQueue() {
    }

    /**
     * Adds a player to the matchmaking queue.
     * <p>
     * If the player is already in the queue, this operation has no effect.
     * Their join time is recorded at the time of insertion.
     *
     * @param player the player to be added to the queue
     */
    public void enqueue(PlayerHandler player) {
        if (queue.add(player)) {
            joinTimes.put(player, System.currentTimeMillis());
        }
    }

    /**
     * Removes a player from the matchmaking queue.
     * <p>
     * Typically used when a player disconnects, cancels matchmaking, or has been successfully matched.
     *
     * @param player the player to be removed from the queue
     */
    public void remove(PlayerHandler player) {
        queue.remove(player);
        joinTimes.remove(player);
    }

    /**
     * Returns the number of players currently waiting in the queue.
     *
     * @return the number of queued players
     */
    public int size() {
        return queue.size();
    }

    /**
     * Checks whether the specified player is currently in the queue.
     *
     * @param player the player to check for presence
     * @return {@code true} if the player is in the queue, {@code false} otherwise
     */
    public boolean contains(PlayerHandler player) {
        return queue.contains(player);
    }

    /**
     * Attempts to match players currently in the queue.
     * <p>
     * Subclasses must implement this method to define how players are matched based on
     * game-specific criteria. This typically includes comparing ratings and evaluating
     * how long each player has been waiting to progressively widen acceptable match conditions.
     */
    public abstract void tryMatch();
}