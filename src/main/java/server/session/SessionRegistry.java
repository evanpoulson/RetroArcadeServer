package server.session;

import server.player.PlayerHandler;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains an in‐memory registry of all active game sessions on the RetroArcade platform.
 * <p>
 * Singleton: use {@link #getInstance()} to access it from anywhere.
 */
public final class SessionRegistry {

    // 1) The one and only instance
    private static final SessionRegistry INSTANCE = new SessionRegistry();

    // 2) Private constructor prevents others from instantiating
    private SessionRegistry() { }

    // 3) Public accessor for the singleton
    public static SessionRegistry getInstance() {
        return INSTANCE;
    }

    /** Maps session IDs to their corresponding {@link SessionContext}. */
    private final ConcurrentHashMap<String, SessionContext> sessions = new ConcurrentHashMap<>();

    /** Maps each active {@link PlayerHandler} to the session ID they are in. */
    private final ConcurrentHashMap<PlayerHandler, String> playerSessionMap = new ConcurrentHashMap<>();

    /** Registers a new game session. */
    public void register(SessionContext context) {
        String sid = context.getSessionID();
        sessions.put(sid, context);
        for (PlayerHandler p : context.getParticipants()) {
            playerSessionMap.put(p, sid);
        }
    }

    /** Deregisters a session by its unique ID. */
    public void deregister(String sessionId) {
        SessionContext ctx = sessions.remove(sessionId);
        if (ctx != null) {
            for (PlayerHandler p : ctx.getParticipants()) {
                playerSessionMap.remove(p);
            }
        }
    }

    /** Lookup by session ID. */
    public Optional<SessionContext> getBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /** Lookup by player handler (for reconnects). */
    public Optional<SessionContext> getByPlayer(PlayerHandler player) {
        String sid = playerSessionMap.get(player);
        return sid == null ? Optional.empty() : getBySessionId(sid);
    }

    /** How many active sessions. */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /** List all active contexts. */
    public Collection<SessionContext> listSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }

    /** Cleanup sessions older than maxAge. */
    public void purgeExpired(Duration maxAge) {
        Instant cutoff = Instant.now().minus(maxAge);
        for (Iterator<Map.Entry<String, SessionContext>> it = sessions.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, SessionContext> e = it.next();
            if (e.getValue().getStartTime().isBefore(cutoff)) {
                it.remove();
                for (PlayerHandler p : e.getValue().getParticipants()) {
                    playerSessionMap.remove(p);
                }
            }
        }
    }
}



