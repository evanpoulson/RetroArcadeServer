package server.matchmaking;

import server.player.PlayerHandler;
import server.utility.GameType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Central orchestrator of all game-specific matchmaking queues.
 * <p>
 * Provides methods to enqueue/remove players and to collect all ready matches
 * across every game type.  Scheduling of matchmaking "ticks" is handled by
 * the caller (e.g. GameCreator), which invokes {@link #collectAllMatches()}
 * on a fixed schedule.
 */
public class MatchmakingManager {

    /**
     * Holds one matchmaking queue per game type.
     */
    private final Map<GameType, MatchmakingQueue> queues;

    /**
     * Carries a matched pair along with the game they should play.
     */
    public static record MatchPairWithType(
            PlayerHandler p1,
            PlayerHandler p2,
            GameType gameType
    ) {}

    /**
     * Initializes one queue for each {@link GameType}.
     */
    public MatchmakingManager() {
        this.queues = new EnumMap<>(GameType.class);
        this.queues.put(GameType.TicTacToe,    new TicTacToeQueue());
        this.queues.put(GameType.ConnectFour,  new ConnectFourQueue());
        this.queues.put(GameType.Checkers,     new CheckersQueue());
    }

    /**
     * Adds the given player to the specified game's queue.
     * <p>
     * Does not itself trigger session creation; the caller must later invoke
     * {@link #collectAllMatches()} to retrieve ready matches.
     *
     * @param gameType the game to join
     * @param player   the player joining the queue
     */
    public void enqueue(GameType gameType, PlayerHandler player) {
        MatchmakingQueue queue = queues.get(gameType);
        if (queue != null) {
            queue.enqueue(player);
        }
    }

    /**
     * Removes the given player from the specified game's queue,
     * for example on disconnect or cancel.
     *
     * @param gameType the game to leave
     * @param player   the player leaving the queue
     */
    public void remove(GameType gameType, PlayerHandler player) {
        MatchmakingQueue queue = queues.get(gameType);
        if (queue != null) {
            queue.remove(player);
        }
    }

    /**
     * Examines each game queue in turn and returns all newly found matches.
     * <p>
     * Each match is returned as a {@link MatchPairWithType}, bundling the two
     * players and the game they should play.  The caller (e.g. GameCreator) can
     * then iterate this list to spawn sessions.
     *
     * @return a list of matched player pairs, empty if none
     */
    public List<MatchPairWithType> collectAllMatches() {
        List<MatchPairWithType> allMatches = new ArrayList<>();
        for (Map.Entry<GameType, MatchmakingQueue> entry : queues.entrySet()) {
            GameType gameType = entry.getKey();
            MatchmakingQueue queue = entry.getValue();
            List<MatchmakingQueue.MatchPair> pairs = queue.tryMatch();
            for (MatchmakingQueue.MatchPair p : pairs) {
                allMatches.add(new MatchPairWithType(p.p1(), p.p2(), gameType));
            }
        }
        return allMatches;
    }
}

