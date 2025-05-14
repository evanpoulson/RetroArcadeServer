package server.session;

import server.player.PlayerHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an in‐memory registry of all active game sessions on the RetroArcade platform.
 * <p>
 * Provides fast lookup of {@link SessionContext} by session ID or by a participating
 * {@link PlayerHandler}, and supports lifecycle operations such as registration,
 * deregistration, and expiration cleanup.
 */
public class SessionRegistry {

    /**
     * Maps session IDs to their corresponding {@link SessionContext}.
     */
    private final ConcurrentHashMap<Integer, SessionContext> sessions = new ConcurrentHashMap<>();

    /**
     * Maps each active {@link PlayerHandler} to the session ID they are currently in.
     * <p>
     * Allows quick reverse lookup for reconnect handling.
     */
    private final ConcurrentHashMap<PlayerHandler, Integer> playerSessionMap = new ConcurrentHashMap<>();

    /**
     * Registers a new game session.
     * <p>
     * Stores the given {@code context} and associates each of its participants
     * with the session ID for quick lookup.
     *
     * @param context the {@link SessionContext} representing the new session
     */
    public void register(SessionContext context) {
        sessions.put(context.sessionId(), context);
        for (PlayerHandler p : context.participants()) {
            playerSessionMap.put(p, context.sessionId());
        }
    }

    /**
     * Deregisters a session by its unique ID.
     * <p>
     * Removes the session and eliminates all reverse mappings for its participants.
     *
     * @param sessionId the unique ID of the session to remove
     */
    public void deregister(Integer sessionId) {
        SessionContext ctx = sessions.remove(sessionId);
        if (ctx != null) {
            for (PlayerHandler p : ctx.participants()) {
                playerSessionMap.remove(p);
            }
        }
    }

    /**
     * Retrieves the active {@link SessionContext} for the given session ID.
     *
     * @param sessionId the session's unique identifier
     * @return an {@link Optional} containing the context if found, or empty if not registered
     */
    public Optional<SessionContext> getBySessionId(Integer sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Retrieves the active {@link SessionContext} that a given player is in.
     * <p>
     * Useful for reconnect logic: given a {@link PlayerHandler}, returns their session.
     *
     * @param player the player handler to look up
     * @return an {@link Optional} containing the context if the player is in a session, or empty otherwise
     */
    public Optional<SessionContext> getByPlayer(PlayerHandler player) {
        Integer sid = playerSessionMap.get(player);
        return sid == null ? Optional.empty() : getBySessionId(sid);
    }

    /**
     * Returns the number of currently active sessions.
     *
     * @return the count of registered sessions
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Returns an unmodifiable view of all active {@link SessionContext} objects.
     *
     * @return a collection of active session contexts
     */
    public Collection<SessionContext> listSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /**
     * Removes any sessions that have exceeded the specified maximum age.
     * <p>
     * Compares each session's {@code startTime} against the current time and
     * deregisters those older than {@code maxAge}. Useful as a safety net
     * to clean up sessions that failed to deregister normally.
     *
     * @param maxAge the maximum allowed duration since session start
     */
    public void purgeExpired(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        for (Map.Entry<Integer, SessionContext> entry : sessions.entrySet()) {
            SessionContext ctx = entry.getValue();
            if (ctx.startTime().isBefore(cutoff)) {
                deregister(ctx.sessionId());
            }
        }
    }
}

