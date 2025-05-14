package server.session;

import server.player.PlayerHandler;
import server.utility.GameType;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import server.utility.ThreadMessage;

/**
 * Holds all the runtime states for one live game session.
 *
 * @param sessionId   a unique ID for this session
 * @param gameType    which GameType (TicTacToe, ConnectFour, Checkers)
 * @param manager     the {@link GameSessionManager} driving this session
 * @param inbox       the BlockingQueue for incoming {@link ThreadMessage}s
 * @param participants the handlers of the players in this session
 * @param startTime   when the session started (for timeouts/metrics)
 * @param state       current lifecycle state (RUNNING, PAUSED, ENDED, etc.)
 */
public record SessionContext(
        String                   sessionId,
        GameType                 gameType,
        GameSessionManager       manager,
        BlockingQueue<ThreadMessage<?>> inbox,
        Set<PlayerHandler>       participants,
        Instant                  startTime,
        SessionState             state
) {}

