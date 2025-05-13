package server.matchmaking;

import server.player.PlayerHandler;

/**
 * Represents an abstract matchmaking queue for a specific game in the multiplayer system.
 * <p>
 * Subclasses of this class are responsible for implementing the logic to match players
 * based on the game type, player availability, or other criteria such as rating.
 * <p>
 * This abstract class defines the essential operations required to manage a queue
 * of players waiting to be matched.
 */
public abstract class MatchmakingQueue {

    /**
     * Adds a player to the matchmaking queue.
     * <p>
     * Subclasses should ensure that duplicate entries are not allowed.
     *
     * @param player the player to be added to the queue
     */
    public abstract void enqueue(PlayerHandler player);

    /**
     * Removes a player from the matchmaking queue.
     * <p>
     * This is typically called when a player disconnects, cancels matchmaking,
     * or is matched and no longer waiting.
     *
     * @param player the player to be removed from the queue
     */
    public abstract void remove(PlayerHandler player);

    /**
     * Attempts to match players currently in the queue.
     * <p>
     * This method should evaluate the queue to find valid player pairs (or groups),
     * and initiate the creation of a new game session if a match is found.
     */
    public abstract void tryMatch();

    /**
     * Returns the number of players currently waiting in the queue.
     *
     * @return the number of queued players
     */
    public abstract int size();

    /**
     * Checks whether the specified player is currently in the queue.
     *
     * @param player the player to check for presence
     * @return {@code true} if the player is in the queue, {@code false} otherwise
     */
    public abstract boolean contains(PlayerHandler player);
}

